package com.Flyway.server.event;

import lombok.Getter;

@Getter
public class BuildUploadedEvent extends DomainEvent {
    
    private final String buildId;
    private final String bundleId;
    private final String commitHash;
    private final String branchName;
    private final String nativeVersion;
    private final Long buildSize;
    
    public BuildUploadedEvent(String buildId, String bundleId, String commitHash, 
                               String branchName, String nativeVersion, Long buildSize,
                               String userId, String organizationId) {
        super(userId, organizationId);
        this.buildId = buildId;
        this.bundleId = bundleId;
        this.commitHash = commitHash;
        this.branchName = branchName;
        this.nativeVersion = nativeVersion;
        this.buildSize = buildSize;
    }
    
    @Override
    public String getEventType() {
        return "build.uploaded";
    }
    
    @Override
    public String getAuditAction() {
        return "BUILD_UPLOADED";
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

