package com.Flyway.Flyway.controller;

import com.Flyway.Flyway.dto.generated.CreateRoleRequest;
import com.Flyway.Flyway.dto.generated.UpdateRoleRequest;
import com.Flyway.Flyway.dto.generated.RoleResponse;
import com.Flyway.Flyway.security.CustomUserDetails;
import com.Flyway.Flyway.security.RequirePermission;
import com.Flyway.Flyway.service.RoleService;
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
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable String id) {
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
    
    @GetMapping("/all")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    
    @PostMapping
    @RequirePermission("role.create")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoleRequest request) {
        RoleResponse role = roleService.updateRole(id, request);
        return ResponseEntity.ok(role);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable String id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok().build();
    }
}

