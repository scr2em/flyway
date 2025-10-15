package com.Flyway.server.event;

import lombok.Getter;

@Getter
public class OrganizationCreatedEvent extends DomainEvent {
    
    private final String organizationName;
    
    public OrganizationCreatedEvent(String organizationId, String organizationName, String userId) {
        super(userId, organizationId);
        this.organizationName = organizationName;
    }
    
    @Override
    public String getEventType() {
        return "organization.created";
    }
    
    @Override
    public String getAuditAction() {
        return "ORGANIZATION_CREATED";
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

