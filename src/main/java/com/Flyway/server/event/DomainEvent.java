package com.Flyway.server.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the system.
 * Events are used to trigger audit logs, webhooks, and notifications.
 */
@Getter
public abstract class DomainEvent {
    
    private final String eventId;
    private final Instant timestamp;
    private final String userId;
    private final String organizationId;
    
    protected DomainEvent(String userId, String organizationId) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.userId = userId;
        this.organizationId = organizationId;
    }
    
    /**
     * Returns the event type identifier (e.g., "mobile_app.created", "build.uploaded")
     * This is used for webhook subscriptions and notification preferences.
     */
    public abstract String getEventType();
    
    /**
     * Returns the action description for audit logs (e.g., "MOBILE_APP_CREATED")
     */
    public abstract String getAuditAction();
    
    /**
     * Returns the resource type (e.g., "MOBILE_APPLICATION", "BUILD")
     */
    public abstract String getResourceType();
    
    /**
     * Returns the resource ID if applicable
     */
    public abstract String getResourceId();
    
    /**
     * Returns the resource name for better readability in audit logs
     */
    public abstract String getResourceName();
}

