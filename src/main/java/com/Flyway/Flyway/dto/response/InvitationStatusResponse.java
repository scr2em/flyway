package com.Flyway.Flyway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationStatusResponse {
    private String id;
    private String code;
    private String label;
    private LocalDateTime createdAt;
}

