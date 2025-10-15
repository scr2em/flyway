package com.Flyway.server.controller;

import com.Flyway.server.exception.BadRequestException;
import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.ApiKeyService;
import com.Flyway.server.service.AppBuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AppBuildController {
    
    private final AppBuildService appBuildService;
    private final ApiKeyService apiKeyService;
    
    /**
     * Upload a new build using API key authentication
     * 
     * This endpoint is designed for CI/CD pipelines and automated build uploads.
     * It does NOT require JWT authentication - only a valid API key in the X-API-Key header.
     * 
     * @param apiKey The API key (from X-API-Key header)
     * @param commitHash The git commit hash (unique identifier for the build)
     * @param branchName The git branch name
     * @param commitMessage The git commit message
     * @param nativeVersion The native app version (e.g., "1.0.0")
     * @param file The build file (APK/IPA)
     */
    @PostMapping(value = "/api/v1/builds", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadBuild(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestParam String commitHash,
            @RequestParam String branchName,
            @RequestParam(required = false) String commitMessage,
            @RequestParam String nativeVersion,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        // Validate API key
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new BadRequestException("X-API-Key header is required");
        }
        
        // Direct lookup of API key
        Map<String, Object> apiKeyData = apiKeyService.lookupApiKey(apiKey);
        
        String bundleId = (String) apiKeyData.get("bundleId");
        String organizationId = (String) apiKeyData.get("organizationId");
        
        // Upload the build (using API key ID as the uploaded_by field)
        Map<String, Object> response = appBuildService.uploadBuildViaApiKey(
                organizationId,
                bundleId,
                commitHash,
                branchName,
                commitMessage,
                nativeVersion,
                (String) apiKeyData.get("id"),
                file
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get builds with pagination and sorting
     * 
     * @param orgId Organization ID
     * @param bundleId Bundle ID of the mobile application
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort direction: "asc" or "desc" (default: "desc")
     */
    @GetMapping("/api/{orgId}/{bundleId}/builds")
    @RequirePermission("build.view")
    public ResponseEntity<Map<String, Object>> getBuilds(
            @PathVariable String orgId,
            @PathVariable String bundleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Map<String, Object> response = appBuildService.getBuilds(
                orgId,
                bundleId,
                page,
                size,
                sort,
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a build by its UUID
     * 
     * @param buildId The UUID of the build to delete
     */
    @DeleteMapping("/api/builds/{buildId}")
    @RequirePermission("build.delete")
    public ResponseEntity<Void> deleteBuild(
            @PathVariable String buildId,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        
        appBuildService.deleteBuild(
                buildId,
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.noContent().build();
    }
}

