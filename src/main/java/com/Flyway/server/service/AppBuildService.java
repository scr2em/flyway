package com.Flyway.server.service;

import com.Flyway.server.dto.generated.BuildResponse;
import com.Flyway.server.dto.generated.PaginatedBuildResponse;
import com.Flyway.server.event.BuildDeletedEvent;
import com.Flyway.server.event.BuildUploadedEvent;
import com.Flyway.server.exception.BadRequestException;
import com.Flyway.server.exception.ConflictException;
import com.Flyway.server.exception.ForbiddenException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.jooq.tables.records.AppBuildsRecord;
import com.Flyway.server.repository.AppBuildRepository;
import com.Flyway.server.repository.MobileApplicationRepository;
import com.Flyway.server.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppBuildService {
    
    private final AppBuildRepository appBuildRepository;
    private final MobileApplicationRepository mobileApplicationRepository;
    private final StorageService storageService;
    private final ApplicationEventPublisher eventPublisher;
    
    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024; // 30MB in bytes
    
    /**
     * Get builds with pagination and sorting
     */
    public PaginatedBuildResponse getBuilds(
            String organizationId,
            String bundleId,
            int page,
            int size,
            String sort,
            String authenticatedUserOrgId) {
        
        // Validate that the user has access to this organization
        if (!organizationId.equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
        // Validate sort parameter
        if (!sort.equalsIgnoreCase("asc") && !sort.equalsIgnoreCase("desc")) {
            throw new BadRequestException("Sort parameter must be either 'asc' or 'desc'");
        }
        
        // Verify the app belongs to the organization
        verifyAppBelongsToOrganization(bundleId, organizationId);
        
        int offset = page * size;
        
        List<AppBuildsRecord> builds = appBuildRepository.findByOrganizationAndBundleId(
                organizationId, bundleId, size, offset, sort);
        int totalCount = appBuildRepository.countByOrganizationAndBundleId(organizationId, bundleId);
        
        List<BuildResponse> buildResponses = builds.stream()
                .map(this::mapToBuildResponse)
                .collect(Collectors.toList());
        
        return new PaginatedBuildResponse()
                .data(buildResponses)
                .page(page)
                .size(size)
                .totalElements(totalCount)
                .totalPages((int) Math.ceil((double) totalCount / size));
        
    }
    

    
    /**
     * Upload a new build via API key (no user auth check needed)
     */
    @Transactional
    public BuildResponse uploadBuildViaApiKey(
            String organizationId,
            String bundleId,
            String commitHash,
            String branchName,
            String commitMessage,
            String nativeVersion,
            String apiKeyId,
            MultipartFile file) throws IOException {
        
        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed size of 30MB");
        }
        
        // Verify the app belongs to the organization
        verifyAppBelongsToOrganization(bundleId, organizationId);
        
        // Check if build already exists
        if (appBuildRepository.exists(organizationId, bundleId, commitHash)) {
            throw new ConflictException("A build with commit hash '" + commitHash + 
                    "' already exists for this application");
        }
        
        // Store the file
        String filePath = String.format("builds/%s/%s/%s/%s", 
                organizationId, bundleId, commitHash, file.getOriginalFilename());
        String buildUrl = storageService.store(file, filePath);
        
        // Create the build record (use apiKeyId as uploadedBy to track API key uploads)
        AppBuildsRecord build;
        try {
            build = appBuildRepository.create(
                    organizationId,
                    bundleId,
                    commitHash,
                    branchName,
                    commitMessage,
                    file.getSize(),
                    buildUrl,
                    nativeVersion,
                    apiKeyId
            );
        } catch (Exception e) {
            // If database insert fails, clean up the uploaded file
            try {
                storageService.delete(filePath);
            } catch (IOException deleteException) {
                // Log but don't throw - the main exception is more important
            }
            throw e;
        }
        
        BuildResponse response = mapToBuildResponse(build);
        
        // Publish event for audit logging, webhooks, and notifications
        eventPublisher.publishEvent(new BuildUploadedEvent(
                build.getId(),
                bundleId,
                commitHash,
                branchName,
                nativeVersion,
                file.getSize(),
                apiKeyId, // Using API key ID as userId for API uploads
                organizationId
        ));
        
        return response;
    }
    
    /**
     * Delete a build by its UUID
     */
    @Transactional
    public void deleteBuild(
            String buildId,
            String authenticatedUserOrgId,
            String userId) throws IOException {
        
        // Find the build
        AppBuildsRecord build = appBuildRepository.findById(buildId)
                .orElseThrow(() -> new ResourceNotFoundException("Build", "id", buildId));
        
        // Validate that the user has access to this organization
        if (!build.getOrganizationId().equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
        // Store build details for the event before deletion
        String bundleId = build.getBundleId();
        String commitHash = build.getCommitHash();
        String organizationId = build.getOrganizationId();
        
        // Delete from storage
        String filePath = extractFilePathFromUrl(
                build.getBuildUrl(), 
                build.getOrganizationId(), 
                build.getBundleId(), 
                build.getCommitHash());
        try {
            storageService.delete(filePath);
        } catch (Exception e) {
            // Log but continue with database deletion
        }
        
        // Delete from database
        appBuildRepository.deleteById(buildId);
        
        // Publish event for audit logging, webhooks, and notifications
        eventPublisher.publishEvent(new BuildDeletedEvent(
                buildId,
                bundleId,
                commitHash,
                userId,
                organizationId
        ));
    }
    
    /**
     * Verify that an app belongs to the given organization
     */
    private void verifyAppBelongsToOrganization(String bundleId, String organizationId) {
        var app = mobileApplicationRepository.findByBundleId(bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Mobile Application", "bundleId", bundleId));
        
        if (!app.getOrganizationId().equals(organizationId)) {
            throw new ForbiddenException("You do not have access to this mobile application");
        }
    }
    
    /**
     * Map database record to response DTO
     */
    private BuildResponse mapToBuildResponse(AppBuildsRecord record) {
        return new BuildResponse()
                .id(record.getId())
                .organizationId(record.getOrganizationId())
                .bundleId(record.getBundleId())
                .commitHash(record.getCommitHash())
                .branchName(record.getBranchName())
                .commitMessage(record.getCommitMessage())
                .buildSize(record.getBuildSize())
                .buildUrl(record.getBuildUrl())
                .nativeVersion(record.getNativeVersion())
                .uploadedBy(record.getUploadedBy())
                .createdAt(record.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(record.getUpdatedAt().atOffset(ZoneOffset.UTC));
    }
      
    
    /**
     * Extract file path from URL for deletion
     */
    private String extractFilePathFromUrl(String url, String organizationId, String bundleId, String commitHash) {
        // Try to extract from URL pattern
        if (url.contains("builds/" + organizationId)) {
            int startIdx = url.indexOf("builds/");
            return url.substring(startIdx);
        }
        
        // Fallback: reconstruct the path (won't work if filename is different)
        return String.format("builds/%s/%s/%s/", organizationId, bundleId, commitHash);
    }
}

