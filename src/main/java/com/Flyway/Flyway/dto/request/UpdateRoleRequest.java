package com.Flyway.Flyway.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    @Size(max = 100, message = "Role name must not exceed 100 characters")
    private String name;
    
    private List<String> permissionIds;
}

