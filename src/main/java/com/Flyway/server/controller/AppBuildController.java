package com.Flyway.server.controller;

import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
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
@RequestMapping("/api/{orgId}/{bundleId}/builds")
@RequiredArgsConstructor
public class AppBuildController {
    
    private final AppBuildService appBuildService;
    
    /**
     * Get builds with pagination and sorting
     * 
     * @param orgId Organization ID
     * @param bundleId Bundle ID of the mobile application
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort direction: "asc" or "desc" (default: "desc")
     */
    @GetMapping
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
     * Upload a new build
     * 
     * @param orgId Organization ID
     * @param bundleId The bundle ID of the mobile application
     * @param commitHash The git commit hash (unique identifier for the build)
     * @param branchName The git branch name
     * @param commitMessage The git commit message
     * @param nativeVersion The native app version (e.g., "1.0.0")
     * @param file The build file (APK/IPA)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission("build.upload")
    public ResponseEntity<Map<String, Object>> uploadBuild(
            @PathVariable String orgId,
            @PathVariable String bundleId,
            @RequestParam String commitHash,
            @RequestParam String branchName,
            @RequestParam(required = false) String commitMessage,
            @RequestParam String nativeVersion,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        
        Map<String, Object> response = appBuildService.uploadBuild(
                orgId,
                bundleId,
                commitHash,
                branchName,
                commitMessage,
                nativeVersion,
                userDetails.getId(),
                file,
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Delete a build
     * 
     * @param orgId Organization ID
     * @param bundleId The bundle ID of the mobile application
     * @param commitHash The commit hash of the build to delete
     */
    @DeleteMapping("/{commitHash}")
    @RequirePermission("build.delete")
    public ResponseEntity<Void> deleteBuild(
            @PathVariable String orgId,
            @PathVariable String bundleId,
            @PathVariable String commitHash,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        
        appBuildService.deleteBuild(
                orgId,
                bundleId,
                commitHash,
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.noContent().build();
    }
}

