package com.Flyway.server.service;

import com.Flyway.server.dto.generated.CreateRoleRequest;
import com.Flyway.server.dto.generated.UpdateRoleRequest;
import com.Flyway.server.dto.generated.PermissionResponse;
import com.Flyway.server.dto.generated.RoleResponse;
import com.Flyway.server.jooq.tables.records.RolesRecord;
import com.Flyway.server.exception.BadRequestException;
import com.Flyway.server.exception.ConflictException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.repository.PermissionRepository;
import com.Flyway.server.repository.RolePermissionRepository;
import com.Flyway.server.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    
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
    
    public List<RoleResponse> getRolesByOrganizationId(String organizationId) {
        return roleRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        // Check if role name already exists in organization
        roleRepository.findByOrganizationIdAndName(request.getOrganizationId(), request.getName())
                .ifPresent(r -> {
                    throw new ConflictException("Role with name '" + request.getName() + "' already exists in this organization");
                });
        
        // Create role
        String roleId = roleRepository.create(request.getOrganizationId(), request.getName(), false, false);
        
        // Assign permissions
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            assignPermissionsToRole(roleId, request.getPermissionIds());
        }
        
        return getRoleById(roleId);
    }
    
    @Transactional
    public RoleResponse updateRole(String id, UpdateRoleRequest request) {
        RolesRecord role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        
        // Check if role is immutable (byte 1 = true)
        if (role.getIsImmutable() != 0) {
            throw new BadRequestException("Cannot update immutable role");
        }
        
        // Update role name if provided
        if (request.getName() != null) {
            roleRepository.update(id, request.getName());
        }
        
        // Update permissions if provided
        if (request.getPermissionIds() != null) {
            // Remove existing permissions
            rolePermissionRepository.deleteByRoleId(id);
            
            // Add new permissions
            if (!request.getPermissionIds().isEmpty()) {
                assignPermissionsToRole(id, request.getPermissionIds());
            }
        }
        
        return getRoleById(id);
    }
    
    @Transactional
    public void deleteRole(String id) {
        RolesRecord role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        
        // Check if role is immutable (byte 1 = true)
        if (role.getIsImmutable() != 0) {
            throw new BadRequestException("Cannot delete immutable role");
        }
        
        roleRepository.delete(id);
    }
    
    private void assignPermissionsToRole(String roleId, List<String> permissionIds) {
        for (String permissionId : permissionIds) {
            // Verify permission exists
            permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));
            
            // Create role-permission association
            rolePermissionRepository.create(roleId, permissionId);
        }
    }
    
    private RoleResponse mapToRoleResponse(RolesRecord record) {
        String roleId = record.getId();
        
        // Fetch permissions for this role
        List<PermissionResponse> permissions = getPermissionsForRole(roleId);
        
        // Convert LocalDateTime to OffsetDateTime
        LocalDateTime createdAtLocal = record.getCreatedAt();
        LocalDateTime updatedAtLocal = record.getUpdatedAt();
        
        return new RoleResponse()
                .id(roleId)
                .organizationId(record.getOrganizationId())
                .name(record.getName())
                .description(null) // Description not stored in database
                .createdAt(createdAtLocal != null ? createdAtLocal.atOffset(ZoneOffset.UTC) : null)
                .updatedAt(updatedAtLocal != null ? updatedAtLocal.atOffset(ZoneOffset.UTC) : null)
                .permissions(permissions);
    }
    
    private List<PermissionResponse> getPermissionsForRole(String roleId) {
        List<Record> records = rolePermissionRepository.findByRoleId(roleId);
        
        List<PermissionResponse> permissions = new ArrayList<>();
        for (Record record : records) {
            String code = record.get("code", String.class);
            String[] codeParts = code != null ? code.split("\\.", 2) : new String[]{"", ""};
            String resource = codeParts.length > 0 ? codeParts[0] : "";
            String action = codeParts.length > 1 ? codeParts[1] : "";
            
            permissions.add(new PermissionResponse()
                    .id(record.get("id", String.class))
                    .name(record.get("label", String.class))
                    .description(record.get("description", String.class))
                    .resource(resource)
                    .action(action));
        }
        
        return permissions;
    }
}

