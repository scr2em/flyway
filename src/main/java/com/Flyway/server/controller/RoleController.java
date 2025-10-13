package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.CreateRoleRequest;
import com.Flyway.server.dto.generated.UpdateRoleRequest;
import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.RoleService;
import com.Flyway.server.dto.generated.RoleResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    @GetMapping("/{id}")
    @RequirePermission("role.view")
    public ResponseEntity<RoleResponse> getRoleById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Verify role belongs to user's organization
        roleService.verifyRoleOwnership(id, userDetails.getOrganizationId());
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }
    
    @GetMapping
    @RequirePermission("role.view")
    public ResponseEntity<List<RoleResponse>> getRoles(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<RoleResponse> roles = roleService.getRolesByOrganizationId(userDetails.getOrganizationId());
        return ResponseEntity.ok(roles);
    }
    
    @PostMapping
    @RequirePermission("role.create")
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody CreateRoleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Get organizationId from authenticated user
        RoleResponse role = roleService.createRole(request, userDetails.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }
    
    @PutMapping("/{id}")
    @RequirePermission("role.update")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Verify role belongs to user's organization
        roleService.verifyRoleOwnership(id, userDetails.getOrganizationId());
        RoleResponse role = roleService.updateRole(id, request);
        return ResponseEntity.ok(role);
    }
    
    @DeleteMapping("/{id}")
    @RequirePermission("role.delete")
    public ResponseEntity<Void> deleteRole(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Verify role belongs to user's organization
        roleService.verifyRoleOwnership(id, userDetails.getOrganizationId());
        roleService.deleteRole(id);
        return ResponseEntity.ok().build();
    }
}

