package com.Flyway.server.controller;

import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ApiKeyController {
    
    private final ApiKeyService apiKeyService;
    
    /**
     * Get all API keys for a specific bundle ID
     * 
     * @param orgId Organization ID
     * @param bundleId Bundle ID of the mobile application
     */
    @GetMapping("/api/{orgId}/{bundleId}/api-keys")
    @RequirePermission("api_key.view")
    public ResponseEntity<List<Map<String, Object>>> getApiKeys(
            @PathVariable String orgId,
            @PathVariable String bundleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        List<Map<String, Object>> apiKeys = apiKeyService.getApiKeysByBundleId(
                bundleId,
                orgId,
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.ok(apiKeys);
    }
    
    /**
     * Create a new API key
     * 
     * @param orgId Organization ID
     * @param bundleId Bundle ID of the mobile application
     * @param name Name/description of the API key
     */
    @PostMapping("/api/{orgId}/{bundleId}/api-keys")
    @RequirePermission("api_key.create")
    public ResponseEntity<Map<String, Object>> createApiKey(
            @PathVariable String orgId,
            @PathVariable String bundleId,
            @RequestParam String name,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Map<String, Object> response = apiKeyService.createApiKey(
                name,
                bundleId,
                orgId,
                userDetails.getId(),
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Delete an API key
     * 
     * @param orgId Organization ID
     * @param bundleId Bundle ID of the mobile application
     * @param keyId The ID of the API key to delete
     */
    @DeleteMapping("/api/{orgId}/{bundleId}/api-keys/{keyId}")
    @RequirePermission("api_key.delete")
    public ResponseEntity<Void> deleteApiKey(
            @PathVariable String orgId,
            @PathVariable String bundleId,
            @PathVariable String keyId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        apiKeyService.deleteApiKey(
                keyId,
                orgId,
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.noContent().build();
    }
}

