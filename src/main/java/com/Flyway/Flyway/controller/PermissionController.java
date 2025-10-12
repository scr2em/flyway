package com.Flyway.Flyway.controller;

import com.Flyway.Flyway.dto.response.ApiResponse;
import com.Flyway.Flyway.dto.response.PermissionResponse;
import com.Flyway.Flyway.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
    
    private final PermissionService permissionService;
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable String id) {
        PermissionResponse permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success(permission));
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionByCode(@PathVariable String code) {
        PermissionResponse permission = permissionService.getPermissionByCode(code);
        return ResponseEntity.ok(ApiResponse.success(permission));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissionsByCategory(
            @PathVariable String category) {
        List<PermissionResponse> permissions = permissionService.getPermissionsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}

