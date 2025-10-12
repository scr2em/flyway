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
public class InvitationResponse {
    private String id;
    private String organizationId;
    private String email;
    private String roleId;
    private String invitedBy;
    private String invitationStatusId;
    private String invitationStatusCode;
    private String invitationStatusLabel;
    private String token;
    private OrganizationResponse organization;
    private RoleResponse role;
    private UserResponse inviter;
    private LocalDateTime expiresAt;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

