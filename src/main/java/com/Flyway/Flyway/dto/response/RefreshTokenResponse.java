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
public class RefreshTokenResponse {
    private String id;
    private String userId;
    private Boolean isRevoked;
    private LocalDateTime revokedAt;
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}

