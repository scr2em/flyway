package com.Flyway.server.service;

import com.Flyway.server.dto.generated.PermissionResponse;
import com.Flyway.server.jooq.tables.records.OrganizationMembersRecord;
import com.Flyway.server.jooq.tables.records.RolesRecord;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.model.Permission;
import com.Flyway.server.repository.OrganizationMemberRepository;
import com.Flyway.server.repository.RoleRepository;
import com.Flyway.server.util.PermissionUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final OrganizationMemberRepository organizationMemberRepository;
    private final RoleRepository roleRepository;
    
    /**
     * Get a permission by its code
     */
    public PermissionResponse getPermissionByCode(String code) {
        Permission permission = Permission.fromCode(code);
        if (permission == null) {
            throw new ResourceNotFoundException("Permission", "code", code);
        }
        
        return mapToPermissionResponse(permission);
    }
    
    /**
     * Get all available permissions
     */
    public List<PermissionResponse> getAllPermissions() {
        return Arrays.stream(Permission.values())
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get permissions by category
     */
    public List<PermissionResponse> getPermissionsByCategory(String category) {
        return Arrays.stream(Permission.values())
                .filter(p -> p.getCategory().equals(category))
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a user has a specific permission in an organization
     * 
     * @param userId The user ID
     * @param organizationId The organization ID
     * @param permissionCode The permission code to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean userHasPermission(String userId, String organizationId, String permissionCode) {
        // First check if user is a member of the organization
        var memberOptional = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId);
        
        if (memberOptional.isEmpty()) {
            return false;
        }
        
        OrganizationMembersRecord member = memberOptional.get();
        String roleId = member.getRoleId();
        
        if (roleId == null) {
            return false;
        }
        
        // Get the role and check permissions using bitwise operations
        var roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            return false;
        }
        
        RolesRecord role = roleOptional.get();
        long rolePermissions = role.getPermissions();
        
        // Check if the role has this permission using bitwise operations
        return PermissionUtil.hasPermission(rolePermissions, permissionCode);
    }
    
    /**
     * Get all permission codes for a user in an organization
     * 
     * @param userId The user ID
     * @param organizationId The organization ID
     * @return List of permission codes the user has
     */
    public List<String> getUserPermissionsInOrganization(String userId, String organizationId) {
        // First check if user is a member of the organization
        var memberOptional = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId);
        
        if (memberOptional.isEmpty()) {
            return List.of();
        }
        
        OrganizationMembersRecord member = memberOptional.get();
        String roleId = member.getRoleId();
        
        if (roleId == null) {
            return List.of();
        }
        
        // Get the role and extract permissions using bitwise operations
        var roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            return List.of();
        }
        
        RolesRecord role = roleOptional.get();
        long rolePermissions = role.getPermissions();
        
        // Convert bitwise permissions to list of codes
        return PermissionUtil.toCodes(rolePermissions);
    }
    
    /**
     * Get all permissions for a role
     * 
     * @param roleId The role ID
     * @return List of permission responses
     */
    public List<PermissionResponse> getPermissionsForRole(String roleId) {
        var roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            return List.of();
        }
        
        RolesRecord role = roleOptional.get();
        long rolePermissions = role.getPermissions();
        
        // Convert bitwise permissions to list of Permission enums
        List<Permission> permissions = PermissionUtil.toPermissions(rolePermissions);
        
        return permissions.stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
    }
    
    private PermissionResponse mapToPermissionResponse(Permission permission) {
        return new PermissionResponse()
                .code(permission.getCode())
                .name(permission.getLabel())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .action(permission.getAction());
    }
}
