package com.Flyway.server.event;

import lombok.Getter;

@Getter
public class BuildDeletedEvent extends DomainEvent {
    
    private final String buildId;
    private final String bundleId;
    private final String commitHash;
    
    public BuildDeletedEvent(String buildId, String bundleId, String commitHash,
                              String userId, String organizationId) {
        super(userId, organizationId);
        this.buildId = buildId;
        this.bundleId = bundleId;
        this.commitHash = commitHash;
    }
    
    @Override
    public String getEventType() {
        return "build.deleted";
    }
    
    @Override
    public String getAuditAction() {
        return "BUILD_DELETED";
    }
    
    @Override
    public String getResourceType() {
        return "BUILD";
    }
    
    @Override
    public String getResourceId() {
        return buildId;
    }
    
    @Override
    public String getResourceName() {
        return String.format("%s@%s", bundleId, commitHash);
    }
}

