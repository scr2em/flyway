package com.Flyway.server.service;

import com.Flyway.server.dto.generated.PermissionResponse;
import com.Flyway.server.jooq.tables.records.OrganizationMembersRecord;
import com.Flyway.server.jooq.tables.records.RolesRecord;
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
     * Get all available permissions in the system
     * 
     * @return List of all permissions
     */
    public List<PermissionResponse> getAllPermissions() {
        return Arrays.stream(Permission.values())
            .map(this::toPermissionResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert Permission enum to PermissionResponse DTO
     */
    private PermissionResponse toPermissionResponse(Permission permission) {
        return new PermissionResponse(
            permission.getCode(),
            permission.getLabel(),
            permission.getDescription(),
            permission.getCategory(),
            String.valueOf(permission.getBitValue())
        );
    }

}
