package com.Flyway.server.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.springframework.stereotype.Repository;

import com.Flyway.server.jooq.tables.records.AuditLogsRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.Flyway.server.jooq.Tables.AUDIT_LOGS;

/**
 * Repository for audit log operations using JOOQ
 */
@Repository
@RequiredArgsConstructor
public class AuditLogRepository {
    
    private final DSLContext dsl;
    
    /**
     * Create a new audit log entry
     */
    public String create(
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
        
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        dsl.insertInto(AUDIT_LOGS)
                .set(AUDIT_LOGS.ID, id)
                .set(AUDIT_LOGS.USER_ID, userId)
                .set(AUDIT_LOGS.ORGANIZATION_ID, organizationId)
                .set(AUDIT_LOGS.ACTION, action)
                .set(AUDIT_LOGS.RESOURCE_TYPE, resourceType)
                .set(AUDIT_LOGS.RESOURCE_ID, resourceId)
                .set(AUDIT_LOGS.RESOURCE_NAME, resourceName)
                .set(AUDIT_LOGS.HTTP_METHOD, httpMethod)
                .set(AUDIT_LOGS.ENDPOINT, endpoint)
                .set(AUDIT_LOGS.IP_ADDRESS, ipAddress)
                .set(AUDIT_LOGS.USER_AGENT, userAgent)
                .set(AUDIT_LOGS.REQUEST_BODY, requestBody)
                .set(AUDIT_LOGS.RESPONSE_STATUS, responseStatus)
                .set(AUDIT_LOGS.ERROR_MESSAGE, errorMessage)
                .set(AUDIT_LOGS.METADATA, metadata != null ? JSON.valueOf(metadata) : null)
                .set(AUDIT_LOGS.CREATED_AT, now)
                .execute();
        
        return id;
    }
    
    /**
     * Find audit logs for an organization with pagination and filtering
     */
    public List<AuditLogsRecord> findByOrganization(
            String organizationId,
            String action,
            String resourceType,
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int limit,
            int offset,
            String sortOrder) {
        
        var query = dsl.selectFrom(AUDIT_LOGS)
                .where(AUDIT_LOGS.ORGANIZATION_ID.eq(organizationId));
        
        // Apply filters if provided
        if (action != null && !action.isEmpty()) {
            query = query.and(AUDIT_LOGS.ACTION.eq(action));
        }
        
        if (resourceType != null && !resourceType.isEmpty()) {
            query = query.and(AUDIT_LOGS.RESOURCE_TYPE.eq(resourceType));
        }
        
        if (userId != null && !userId.isEmpty()) {
            query = query.and(AUDIT_LOGS.USER_ID.eq(userId));
        }
        
        if (startDate != null) {
            query = query.and(AUDIT_LOGS.CREATED_AT.greaterOrEqual(startDate));
        }
        
        if (endDate != null) {
            query = query.and(AUDIT_LOGS.CREATED_AT.lessOrEqual(endDate));
        }
        
        // Apply sorting and return
        if ("asc".equalsIgnoreCase(sortOrder)) {
            return query.orderBy(AUDIT_LOGS.CREATED_AT.asc())
                    .limit(limit)
                    .offset(offset)
                    .fetch();
        } else {
            return query.orderBy(AUDIT_LOGS.CREATED_AT.desc())
                    .limit(limit)
                    .offset(offset)
                    .fetch();
        }
    }
    
    /**
     * Count audit logs for an organization with filters
     */
    public int countByOrganization(
            String organizationId,
            String action,
            String resourceType,
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        var query = dsl.selectCount()
                .from(AUDIT_LOGS)
                .where(AUDIT_LOGS.ORGANIZATION_ID.eq(organizationId));
        
        // Apply filters if provided
        if (action != null && !action.isEmpty()) {
            query = query.and(AUDIT_LOGS.ACTION.eq(action));
        }
        
        if (resourceType != null && !resourceType.isEmpty()) {
            query = query.and(AUDIT_LOGS.RESOURCE_TYPE.eq(resourceType));
        }
        
        if (userId != null && !userId.isEmpty()) {
            query = query.and(AUDIT_LOGS.USER_ID.eq(userId));
        }
        
        if (startDate != null) {
            query = query.and(AUDIT_LOGS.CREATED_AT.greaterOrEqual(startDate));
        }
        
        if (endDate != null) {
            query = query.and(AUDIT_LOGS.CREATED_AT.lessOrEqual(endDate));
        }
        
        return query.fetchOne(0, int.class);
    }
    
    /**
     * Find audit logs for a specific resource
     */
    public List<AuditLogsRecord> findByResource(
            String organizationId,
            String resourceType,
            String resourceId,
            int limit,
            int offset) {
        
        return dsl.selectFrom(AUDIT_LOGS)
                .where(AUDIT_LOGS.ORGANIZATION_ID.eq(organizationId))
                .and(AUDIT_LOGS.RESOURCE_TYPE.eq(resourceType))
                .and(AUDIT_LOGS.RESOURCE_ID.eq(resourceId))
                .orderBy(AUDIT_LOGS.CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch();
    }
    
    /**
     * Find recent audit logs for a user
     */
    public List<AuditLogsRecord> findByUser(String userId, int limit) {
        return dsl.selectFrom(AUDIT_LOGS)
                .where(AUDIT_LOGS.USER_ID.eq(userId))
                .orderBy(AUDIT_LOGS.CREATED_AT.desc())
                .limit(limit)
                .fetch();
    }
}

