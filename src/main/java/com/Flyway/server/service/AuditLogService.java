package com.Flyway.server.service;

import com.Flyway.server.event.DomainEvent;
import com.Flyway.server.jooq.tables.records.AuditLogsRecord;
import com.Flyway.server.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing audit logs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    /**
     * Create an audit log entry from a domain event
     */
    @Transactional
    public String logEvent(DomainEvent event) {
        return logEvent(
                event.getUserId(),
                event.getOrganizationId(),
                event.getAuditAction(),
                event.getResourceType(),
                event.getResourceId(),
                event.getResourceName(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
    
    /**
     * Create an audit log entry with full details
     */
    @Transactional
    public String logEvent(
            String userId,
            String organizationId,
            String action,
            String resourceType,
            String resourceId,
            String resourceName,
            String httpMethod,
            String endpoint,
            String ipAddress,
            String userAgent,
            String requestBody,
            Integer responseStatus,
            String errorMessage,
            String metadata) {
        
        try {
            return auditLogRepository.create(
                    userId,
                    organizationId,
                    action,
                    resourceType,
                    resourceId,
                    resourceName,
                    httpMethod,
                    endpoint,
                    ipAddress,
                    userAgent,
                    requestBody,
                    responseStatus,
                    errorMessage,
                    metadata
            );
        } catch (Exception e) {
            // Log the error but don't throw - audit logging shouldn't break the main flow
            log.error("Failed to create audit log entry: action={}, resourceType={}, resourceId={}", 
                      action, resourceType, resourceId, e);
            return null;
        }
    }
    
    /**
     * Get audit logs for an organization with pagination and filtering
     */
    public List<AuditLogsRecord> getAuditLogs(
            String organizationId,
            String action,
            String resourceType,
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sort) {
        
        int offset = page * size;
        
        return auditLogRepository.findByOrganization(
                organizationId,
                action,
                resourceType,
                userId,
                startDate,
                endDate,
                size,
                offset,
                sort
        );
    }
    
    /**
     * Count audit logs for an organization with filters
     */
    public int countAuditLogs(
            String organizationId,
            String action,
            String resourceType,
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        return auditLogRepository.countByOrganization(
                organizationId,
                action,
                resourceType,
                userId,
                startDate,
                endDate
        );
    }
    
    /**
     * Get audit logs for a specific resource
     */
    public List<AuditLogsRecord> getResourceAuditLogs(
            String organizationId,
            String resourceType,
            String resourceId,
            int page,
            int size) {
        
        int offset = page * size;
        
        return auditLogRepository.findByResource(
                organizationId,
                resourceType,
                resourceId,
                size,
                offset
        );
    }
    
    /**
     * Get recent audit logs for a user
     */
    public List<AuditLogsRecord> getUserRecentLogs(String userId, int limit) {
        return auditLogRepository.findByUser(userId, limit);
    }
}

