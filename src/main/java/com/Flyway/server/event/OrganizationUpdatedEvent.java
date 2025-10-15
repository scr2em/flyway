package com.Flyway.server.event;

import lombok.Getter;

@Getter
public class OrganizationUpdatedEvent extends DomainEvent {
    
    private final String organizationName;
    
    public OrganizationUpdatedEvent(String organizationId, String organizationName, String userId) {
        super(userId, organizationId);
        this.organizationName = organizationName;
    }
    
    @Override
    public String getEventType() {
        return "organization.updated";
    }
    
    @Override
    public String getAuditAction() {
        return "ORGANIZATION_UPDATED";
    }
    
    @Override
    public String getResourceType() {
        return "ORGANIZATION";
    }
    
    @Override
    public String getResourceId() {
        return getOrganizationId();
    }
    
    @Override
    public String getResourceName() {
        return organizationName;
    }
}

