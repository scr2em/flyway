package com.Flyway.Flyway.service;

import com.Flyway.Flyway.dto.request.CreateRoleRequest;
import com.Flyway.Flyway.dto.request.UpdateRoleRequest;
import com.Flyway.Flyway.dto.response.PermissionResponse;
import com.Flyway.Flyway.dto.response.RoleResponse;
import com.Flyway.Flyway.exception.BadRequestException;
import com.Flyway.Flyway.exception.ConflictException;
import com.Flyway.Flyway.exception.ResourceNotFoundException;
import com.Flyway.Flyway.repository.PermissionRepository;
import com.Flyway.Flyway.repository.RolePermissionRepository;
import com.Flyway.Flyway.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        Record role = roleRepository.findById(id)
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
        Record role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        
        // Check if role is immutable
        if (role.get("is_immutable", Boolean.class)) {
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
        Record role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        
        // Check if role is immutable
        if (role.get("is_immutable", Boolean.class)) {
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
    
    private RoleResponse mapToRoleResponse(Record record) {
        String roleId = record.get("id", String.class);
        
        // Fetch permissions for this role
        List<PermissionResponse> permissions = getPermissionsForRole(roleId);
        
        return RoleResponse.builder()
                .id(roleId)
                .organizationId(record.get("organization_id", String.class))
                .name(record.get("name", String.class))
                .isSystemRole(record.get("is_system_role", Boolean.class))
                .isImmutable(record.get("is_immutable", Boolean.class))
                .createdAt(record.get("created_at", LocalDateTime.class))
                .updatedAt(record.get("updated_at", LocalDateTime.class))
                .permissions(permissions)
                .build();
    }
    
    private List<PermissionResponse> getPermissionsForRole(String roleId) {
        List<Record> records = rolePermissionRepository.findByRoleId(roleId);
        
        List<PermissionResponse> permissions = new ArrayList<>();
        for (Record record : records) {
            permissions.add(PermissionResponse.builder()
                    .id(record.get("id", String.class))
                    .code(record.get("code", String.class))
                    .label(record.get("label", String.class))
                    .description(record.get("description", String.class))
                    .category(record.get("category", String.class))
                    .createdAt(record.get("created_at", LocalDateTime.class))
                    .build());
        }
        
        return permissions;
    }
}

