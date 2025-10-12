package com.Flyway.Flyway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationMemberResponse {
    private String id;
    private String organizationId;
    private String userId;
    private String roleId;
    private UserResponse user;
    private RoleResponse role;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
}

