package com.Flyway.server.event;

import lombok.Getter;

@Getter
public class MobileAppUpdatedEvent extends DomainEvent {
    
    private final String appId;
    private final String appName;
    private final String bundleId;
    
    public MobileAppUpdatedEvent(String appId, String appName, String bundleId,
                                  String userId, String organizationId) {
        super(userId, organizationId);
        this.appId = appId;
        this.appName = appName;
        this.bundleId = bundleId;
    }
    
    @Override
    public String getEventType() {
        return "mobile_app.updated";
    }
    
    @Override
    public String getAuditAction() {
        return "MOBILE_APP_UPDATED";
    }
    
    @Override
    public String getResourceType() {
        return "MOBILE_APPLICATION";
    }
    
    @Override
    public String getResourceId() {
        return appId;
    }
    
    @Override
    public String getResourceName() {
        return appName;
    }
}

