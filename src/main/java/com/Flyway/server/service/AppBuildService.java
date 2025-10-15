package com.Flyway.server.service;

import com.Flyway.server.exception.BadRequestException;
import com.Flyway.server.exception.ConflictException;
import com.Flyway.server.exception.ForbiddenException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.jooq.tables.records.AppBuildsRecord;
import com.Flyway.server.repository.AppBuildRepository;
import com.Flyway.server.repository.MobileApplicationRepository;
import com.Flyway.server.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppBuildService {
    
    private final AppBuildRepository appBuildRepository;
    private final MobileApplicationRepository mobileApplicationRepository;
    private final StorageService storageService;
    
    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024; // 30MB in bytes
    
    /**
     * Get builds with pagination and sorting
     */
    public Map<String, Object> getBuilds(
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
        
        List<Map<String, Object>> buildResponses = builds.stream()
                .map(this::mapToBuildResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", buildResponses);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", totalCount);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        return response;
    }
    
    /**
     * Upload a new build
     */
    @Transactional
    public Map<String, Object> uploadBuild(
            String organizationId,
            String bundleId,
            String commitHash,
            String branchName,
            String commitMessage,
            String nativeVersion,
            String userId,
            MultipartFile file,
            String authenticatedUserOrgId) throws IOException {
        
        // Validate that the user has access to this organization
        if (!organizationId.equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
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
        
        // Create the build record
        try {
            appBuildRepository.create(
                    organizationId,
                    bundleId,
                    commitHash,
                    branchName,
                    commitMessage,
                    file.getSize(),
                    buildUrl,
                    nativeVersion,
                    userId
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
        
        // Fetch and return the created build
        AppBuildsRecord build = appBuildRepository.findByKey(organizationId, bundleId, commitHash)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve created build"));
        
        return mapToBuildResponse(build);
    }
    
    /**
     * Delete a build
     */
    @Transactional
    public void deleteBuild(
            String organizationId,
            String bundleId,
            String commitHash,
            String authenticatedUserOrgId) throws IOException {
        
        // Validate that the user has access to this organization
        if (!organizationId.equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
        // Verify the app belongs to the organization
        verifyAppBelongsToOrganization(bundleId, organizationId);
        
        // Check if build exists
        AppBuildsRecord build = appBuildRepository.findByKey(organizationId, bundleId, commitHash)
                .orElseThrow(() -> new ResourceNotFoundException("Build", "commitHash", commitHash));
        
        // Delete from storage
        String filePath = extractFilePathFromUrl(build.getBuildUrl(), organizationId, bundleId, commitHash);
        try {
            storageService.delete(filePath);
        } catch (Exception e) {
            // Log but continue with database deletion
        }
        
        // Delete from database
        appBuildRepository.delete(organizationId, bundleId, commitHash);
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
    private Map<String, Object> mapToBuildResponse(AppBuildsRecord record) {
        Map<String, Object> response = new HashMap<>();
        response.put("organizationId", record.getOrganizationId());
        response.put("bundleId", record.getBundleId());
        response.put("commitHash", record.getCommitHash());
        response.put("branchName", record.getBranchName());
        response.put("commitMessage", record.getCommitMessage());
        response.put("buildSize", record.getBuildSize());
        response.put("buildUrl", record.getBuildUrl());
        response.put("nativeVersion", record.getNativeVersion());
        response.put("uploadedBy", record.getUploadedBy());
        response.put("createdAt", Date.from(record.getCreatedAt().toInstant(ZoneOffset.UTC)));
        response.put("updatedAt", Date.from(record.getUpdatedAt().toInstant(ZoneOffset.UTC)));
        return response;
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

