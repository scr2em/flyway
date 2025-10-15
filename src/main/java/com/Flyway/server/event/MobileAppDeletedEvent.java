package com.Flyway.server.event;

import lombok.Getter;

@Getter
public class MobileAppDeletedEvent extends DomainEvent {
    
    private final String appId;
    private final String appName;
    private final String bundleId;
    
    public MobileAppDeletedEvent(String appId, String appName, String bundleId,
                                  String userId, String organizationId) {
        super(userId, organizationId);
        this.appId = appId;
        this.appName = appName;
        this.bundleId = bundleId;
    }
    
    @Override
    public String getEventType() {
        return "mobile_app.deleted";
    }
    
    @Override
    public String getAuditAction() {
        return "MOBILE_APP_DELETED";
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

