package com.Flyway.Flyway.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddOrganizationMemberRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Role ID is required")
    private String roleId;
}

