package com.Flyway.server.service;

import com.Flyway.server.jooq.tables.records.OrganizationMembersRecord;
import com.Flyway.server.jooq.tables.records.RolesRecord;
import com.Flyway.server.repository.OrganizationMemberRepository;
import com.Flyway.server.repository.RoleRepository;
import com.Flyway.server.util.PermissionUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
 
}
