package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.PermissionResponse;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.PermissionService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
    
    private final PermissionService permissionService;
    
    // Permissions are system-wide, so we require permission.view to see them
    @GetMapping("/{id}")
    @RequirePermission("permission.view")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable String id) {
        PermissionResponse permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permission);
    }
    
    @GetMapping("/code/{code}")
    @RequirePermission("permission.view")
    public ResponseEntity<PermissionResponse> getPermissionByCode(@PathVariable String code) {
        PermissionResponse permission = permissionService.getPermissionByCode(code);
        return ResponseEntity.ok(permission);
    }
    
    @GetMapping
    @RequirePermission("permission.view")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }
    
    @GetMapping("/category/{category}")
    @RequirePermission("permission.view")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByCategory(
            @PathVariable String category) {
        List<PermissionResponse> permissions = permissionService.getPermissionsByCategory(category);
        return ResponseEntity.ok(permissions);
    }
}

