package com.Flyway.server.controller;

import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.RoleService;
import com.Flyway.server.dto.generated.RoleResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    @GetMapping
    @RequirePermission("role.view")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        // Roles are now global, return all available roles
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
