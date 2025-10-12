package com.Flyway.server.service;

import com.Flyway.server.dto.generated.PermissionResponse;
import com.Flyway.server.jooq.tables.records.OrganizationMembersRecord;
import com.Flyway.server.jooq.tables.records.PermissionsRecord;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.repository.OrganizationMemberRepository;
import com.Flyway.server.repository.PermissionRepository;
import com.Flyway.server.repository.RolePermissionRepository;

import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final RolePermissionRepository rolePermissionRepository;
    
    public PermissionResponse getPermissionById(String id) {
        PermissionsRecord permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
        
        return mapToPermissionResponse(permission);
    }
    
    public PermissionResponse getPermissionByCode(String code) {
        PermissionsRecord permission = permissionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "code", code));
        
        return mapToPermissionResponse(permission);
    }
    
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
    }
    
    public List<PermissionResponse> getPermissionsByCategory(String category) {
        return permissionRepository.findByCategory(category).stream()
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
        
        // Get permission by code
        var permissionOptional = permissionRepository.findByCode(permissionCode);
        if (permissionOptional.isEmpty()) {
            return false;
        }
        
        String permissionId = permissionOptional.get().getId();
        
        // Check if the role has this permission
        return rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId);
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
        
        // Get all permissions for the role
        List<Record> rolePermissions = rolePermissionRepository.findByRoleId(roleId);
        
        return rolePermissions.stream()
                .map(record -> record.get("code", String.class))
                .collect(Collectors.toList());
    }
    
    private PermissionResponse mapToPermissionResponse(PermissionsRecord record) {
        String code = record.getCode();
        String[] codeParts = code != null ? code.split("\\.", 2) : new String[]{"", ""};
        String resource = codeParts.length > 0 ? codeParts[0] : "";
        String action = codeParts.length > 1 ? codeParts[1] : "";
        
        return new PermissionResponse()
                .id(record.getId())
                .name(record.getLabel())
                .description(record.getDescription())
                .resource(resource)
                .action(action);
    }
}

