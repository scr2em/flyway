package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.AuditLogResponse;
import com.Flyway.server.dto.generated.PaginatedAuditLogResponse;
import com.Flyway.server.jooq.tables.records.AuditLogsRecord;
import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing and querying audit logs.
 * 
 * Audit logs track all significant actions in the system including:
 * - Mobile application operations (create, update, delete)
 * - Build operations (upload, delete)
 * - Organization operations (create, update)
 * - And more...
 * 
 * Future: This event system is designed to support webhooks and notifications
 * by simply adding additional event listeners.
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    
    private final AuditLogService auditLogService;
    
    /**
     * Get audit logs for the authenticated user's organization with filtering and pagination.
     * 
     * @param action Filter by action (e.g., "MOBILE_APP_CREATED")
     * @param resourceType Filter by resource type (e.g., "MOBILE_APPLICATION")
     * @param userId Filter by user ID
     * @param startDate Filter by start date
     * @param endDate Filter by end date
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param sort Sort order (asc or desc)
     */
    @GetMapping
    @RequirePermission("audit.read")
    public ResponseEntity<PaginatedAuditLogResponse> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        String organizationId = userDetails.getOrganizationId();
        
        // Convert OffsetDateTime to LocalDateTime for JOOQ queries
        LocalDateTime startLocalDateTime = startDate != null ? startDate.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
        LocalDateTime endLocalDateTime = endDate != null ? endDate.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
        
        // Get audit logs
        List<AuditLogsRecord> records = auditLogService.getAuditLogs(
                organizationId,
                action,
                resourceType,
                userId,
                startLocalDateTime,
                endLocalDateTime,
                page,
                size,
                sort
        );
        
        // Get total count
        int totalCount = auditLogService.countAuditLogs(
                organizationId,
                action,
                resourceType,
                userId,
                startLocalDateTime,
                endLocalDateTime
        );
        
        // Map to response DTOs
        List<AuditLogResponse> auditLogResponses = records.stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
        
        // Build paginated response
        PaginatedAuditLogResponse response = new PaginatedAuditLogResponse()
                .data(auditLogResponses)
                .page(page)
                .size(size)
                .totalElements(totalCount)
                .totalPages((int) Math.ceil((double) totalCount / size));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Map JOOQ Record to AuditLogResponse DTO
     */
    private AuditLogResponse mapToAuditLogResponse(AuditLogsRecord auditLog) {
        return new AuditLogResponse()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .organizationId(auditLog.getOrganizationId())
                .action(auditLog.getAction())
                .resourceType(auditLog.getResourceType())
                .resourceId(auditLog.getResourceId())
                .resourceName(auditLog.getResourceName())
                .httpMethod(auditLog.getHttpMethod())
                .endpoint(auditLog.getEndpoint())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .requestBody(auditLog.getRequestBody())
                .responseStatus(auditLog.getResponseStatus())
                .errorMessage(auditLog.getErrorMessage())
                .metadata(auditLog.getMetadata() != null ? auditLog.getMetadata().data() : null)
                .createdAt(auditLog.getCreatedAt() != null ? 
                           auditLog.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
    }
}

