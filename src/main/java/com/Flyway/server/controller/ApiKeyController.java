package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.ApiKeyResponse;
import com.Flyway.server.dto.generated.CreateApiKeyRequest;
import com.Flyway.server.dto.generated.PaginatedApiKeyResponse;
import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.ApiKeyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ApiKeyController {
    
    private final ApiKeyService apiKeyService;
    
    /**
     * Get all API keys for a specific bundle ID with pagination
     * 
     * @param orgId Organization ID
     * @param bundleId Bundle ID of the mobile application
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort direction: "asc" or "desc" (default: "desc")
     */
    @GetMapping("/api/{orgId}/{bundleId}/api-keys")
    @RequirePermission("api_key.view")
    public ResponseEntity<PaginatedApiKeyResponse> getApiKeys(
            @PathVariable String orgId,
            @PathVariable String bundleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
            PaginatedApiKeyResponse response = apiKeyService.getApiKeysByBundleIdPaginated(
                bundleId,
                orgId,
                page,
                size,
                sort,
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.ok(response);
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
    public ResponseEntity<ApiKeyResponse> createApiKey(
            @PathVariable String orgId,
            @PathVariable String bundleId,
            @Valid @RequestBody CreateApiKeyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        ApiKeyResponse response = apiKeyService.createApiKey(
                request.getName(),
                request.getBundleId(),
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
            @PathVariable String keyId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        apiKeyService.deleteApiKey(keyId);
        
        return ResponseEntity.noContent().build();
    }
}

