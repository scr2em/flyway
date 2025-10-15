package com.Flyway.server.service;

import com.Flyway.server.dto.generated.CreateRoleRequest;
import com.Flyway.server.dto.generated.UpdateRoleRequest;
import com.Flyway.server.dto.generated.PermissionResponse;
import com.Flyway.server.dto.generated.RoleResponse;
import com.Flyway.server.jooq.tables.records.RolesRecord;
import com.Flyway.server.exception.BadRequestException;
import com.Flyway.server.exception.ConflictException;
import com.Flyway.server.exception.ForbiddenException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.repository.OrganizationMemberRepository;
import com.Flyway.server.repository.RoleRepository;
import com.Flyway.server.util.PermissionUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final PermissionService permissionService;
    
    public RoleResponse getRoleById(String id) {
        RolesRecord role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        
        return mapToRoleResponse(role);
    }
    
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        // Check if role name already exists (roles are now global)
        roleRepository.findByName(request.getName())
                .ifPresent(r -> {
                    throw new ConflictException("Role with name '" + request.getName() + "' already exists");
                });
        
        // Parse permissions from string
        long permissions = PermissionUtil.parsePermissionString(request.getPermissions());
        
        // Create role
        String roleId = roleRepository.create(
            request.getName(), 
            request.getDescription(),
            permissions
        );
        
        return getRoleById(roleId);
    }
    
    @Transactional
    public RoleResponse updateRole(String id, UpdateRoleRequest request) {
        roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        
        // Parse permissions if provided
        Long permissions = null;
        if (request.getPermissions() != null) {
            permissions = PermissionUtil.parsePermissionString(request.getPermissions());
        }
        
        // Update role
        roleRepository.update(id, request.getName(), request.getDescription(), permissions);
        
        return getRoleById(id);
    }
    
    @Transactional
    public void deleteRole(String id) {
        roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        
        // Check if role has any members assigned to it
        if (!organizationMemberRepository.findByRoleId(id).isEmpty()) {
            throw new ConflictException("Cannot delete role that has members assigned to it. Please reassign or remove members first.");
        }
        
        roleRepository.delete(id);
    }
    
    private RoleResponse mapToRoleResponse(RolesRecord record) {
        String roleId = record.getId();
        
        // Fetch permissions for this role
        List<PermissionResponse> permissions = permissionService.getPermissionsForRole(roleId);
        
        // Convert LocalDateTime to OffsetDateTime
        LocalDateTime createdAtLocal = record.getCreatedAt();
        LocalDateTime updatedAtLocal = record.getUpdatedAt();
        
        // Get permissions value as string
        String permissionsValue = PermissionUtil.toPermissionString(record.getPermissions());
        
        return new RoleResponse()
                .id(roleId)
                .name(record.getName())
                .description(record.getDescription())
                .permissionsValue(permissionsValue)
                .permissions(permissions)
                .createdAt(createdAtLocal != null ? createdAtLocal.atOffset(ZoneOffset.UTC) : null)
                .updatedAt(updatedAtLocal != null ? updatedAtLocal.atOffset(ZoneOffset.UTC) : null);
    }
}
