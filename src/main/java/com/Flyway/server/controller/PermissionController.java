package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.PermissionResponse;
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
    
    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }
}

