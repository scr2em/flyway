package com.Flyway.server.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Model class for audit log entries
 */
@Getter
@Builder
public class AuditLog {
    private String id;
    private String userId;
    private String organizationId;
    private String action;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    private String httpMethod;
    private String endpoint;
    private String ipAddress;
    private String userAgent;
    private String requestBody;
    private Integer responseStatus;
    private String errorMessage;
    private String metadata;
    private LocalDateTime createdAt;
}

