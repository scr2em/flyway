package com.Flyway.Flyway.service;

import com.Flyway.Flyway.dto.response.PermissionResponse;
import com.Flyway.Flyway.exception.ResourceNotFoundException;
import com.Flyway.Flyway.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    
    public PermissionResponse getPermissionById(String id) {
        Record permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
        
        return mapToPermissionResponse(permission);
    }
    
    public PermissionResponse getPermissionByCode(String code) {
        Record permission = permissionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "code", code));
        
        return mapToPermissionResponse(permission);
    }
    
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
    }
    
    public List<PermissionResponse> getPermissionsByCategory(String category) {
        return permissionRepository.findByCategory(category).stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
    }
    
    private PermissionResponse mapToPermissionResponse(Record record) {
        return PermissionResponse.builder()
                .id(record.get("id", String.class))
                .code(record.get("code", String.class))
                .label(record.get("label", String.class))
                .description(record.get("description", String.class))
                .category(record.get("category", String.class))
                .createdAt(record.get("created_at", LocalDateTime.class))
                .build();
    }
}

