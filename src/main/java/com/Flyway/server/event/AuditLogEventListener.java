package com.Flyway.server.event;

import com.Flyway.server.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener that creates audit log entries for all domain events.
 * This listener processes events asynchronously to not block the main request.
 * 
 * To add webhook or notification support in the future, simply create additional
 * @EventListener methods for the specific events you want to handle.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogEventListener {
    
    private final AuditLogService auditLogService;
    
    /**
     * Listen to all domain events and create audit log entries.
     * This method is async so it won't block the main request thread.
     */
    @Async
    @EventListener
    public void handleDomainEvent(DomainEvent event) {
        try {
            log.debug("Processing audit log for event: {} ({})", 
                      event.getAuditAction(), event.getEventId());
            
            auditLogService.logEvent(event);
            
            log.debug("Audit log created for event: {} - resourceType={}, resourceId={}", 
                      event.getAuditAction(), 
                      event.getResourceType(), 
                      event.getResourceId());
            
        } catch (Exception e) {
            // Log the error but don't throw - audit logging shouldn't break the main flow
            log.error("Failed to process audit log for event: {} - {}", 
                      event.getAuditAction(), e.getMessage(), e);
        }
    }
    
    // Future webhook listener example:
    // @Async
    // @EventListener
    // public void handleEventForWebhooks(DomainEvent event) {
    //     // Find webhook subscriptions for this event type
    //     // Send webhook payloads to subscribed URLs
    //     // Handle retries and delivery tracking
    // }
    
    // Future notification listener example:
    // @Async
    // @EventListener
    // public void handleEventForNotifications(DomainEvent event) {
    //     // Find users who should be notified about this event
    //     // Check their notification preferences
    //     // Create in-app notifications
    //     // Send emails if opted in
    // }
}

