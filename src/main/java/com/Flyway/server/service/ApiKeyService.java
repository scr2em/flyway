package com.Flyway.server.service;

import com.Flyway.server.exception.BadRequestException;
import com.Flyway.server.exception.ForbiddenException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.jooq.tables.records.ApiKeysRecord;
import com.Flyway.server.repository.ApiKeyRepository;
import com.Flyway.server.repository.MobileApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyService {
    
    private final ApiKeyRepository apiKeyRepository;
    private final MobileApplicationRepository mobileApplicationRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final String API_KEY_PREFIX = "flyway_";
    private static final int KEY_LENGTH = 32;
    private static final String ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    /**
     * Create a new API key
     */
    @Transactional
    public Map<String, Object> createApiKey(
            String name,
            String bundleId,
            String organizationId,
            String createdBy,
            String authenticatedUserOrgId) {
        
        // Validate that the user has access to this organization
        if (!organizationId.equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
        // Verify the app belongs to the organization
        verifyAppBelongsToOrganization(bundleId, organizationId);
        
        // Generate the API key
        String apiKey = generateApiKey();
        String keyHash = passwordEncoder.encode(apiKey);
        String keyPrefix = apiKey.substring(0, Math.min(15, apiKey.length()));
        
        // Ensure the prefix is unique (highly unlikely to collide, but check anyway)
        int attempts = 0;
        while (apiKeyRepository.existsByKeyPrefix(keyPrefix) && attempts < 5) {
            apiKey = generateApiKey();
            keyHash = passwordEncoder.encode(apiKey);
            keyPrefix = apiKey.substring(0, Math.min(15, apiKey.length()));
            attempts++;
        }
        
        if (attempts >= 5) {
            throw new RuntimeException("Failed to generate unique API key prefix");
        }
        
        // Create the API key record (no expiration)
        String id = UUID.randomUUID().toString();
        ApiKeysRecord record = apiKeyRepository.create(
                id,
                keyHash,
                keyPrefix,
                name,
                bundleId,
                organizationId,
                createdBy,
                null  // API keys never expire
        );
        
        // Return the response with the plain API key (only time it will be visible)
        Map<String, Object> response = mapToApiKeyResponse(record);
        response.put("key", apiKey); // Include the plain key only on creation
        
        return response;
    }
    
    /**
     * Get all API keys for a bundle ID and organization
     */
    public List<Map<String, Object>> getApiKeysByBundleId(
            String bundleId,
            String organizationId,
            String authenticatedUserOrgId) {
        
        // Validate that the user has access to this organization
        if (!organizationId.equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
        // Verify the app belongs to the organization
        verifyAppBelongsToOrganization(bundleId, organizationId);
        
        List<ApiKeysRecord> apiKeys = apiKeyRepository.findByBundleIdAndOrganizationId(bundleId, organizationId);
        
        return apiKeys.stream()
                .map(this::mapToApiKeyResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all API keys for an organization
     */
    public List<Map<String, Object>> getApiKeysByOrganizationId(
            String organizationId,
            String authenticatedUserOrgId) {
        
        // Validate that the user has access to this organization
        if (!organizationId.equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
        List<ApiKeysRecord> apiKeys = apiKeyRepository.findByOrganizationId(organizationId);
        
        return apiKeys.stream()
                .map(this::mapToApiKeyResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete an API key
     */
    @Transactional
    public void deleteApiKey(
            String id,
            String organizationId,
            String authenticatedUserOrgId) {
        
        // Validate that the user has access to this organization
        if (!organizationId.equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
        // Find the API key
        ApiKeysRecord apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API key not found"));
        
        // Verify it belongs to the organization
        if (!apiKey.getOrganizationId().equals(organizationId)) {
            throw new ForbiddenException("You do not have access to this API key");
        }
        
        // Delete the API key
        apiKeyRepository.delete(id);
    }
    
    /**
     * Direct lookup of API key by exact value
     */
    public Map<String, Object> lookupApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new BadRequestException("API key is required");
        }
        
        // Extract the prefix to narrow down candidates
        String keyPrefix = apiKey.substring(0, Math.min(15, apiKey.length()));
        
        // Find all API keys with this prefix (should be unique)
        List<ApiKeysRecord> candidates = apiKeyRepository.findByKeyPrefix(keyPrefix);
        
        if (candidates.isEmpty()) {
            throw new BadRequestException("Invalid API key");
        }
        
        // Verify the plain API key against the stored hashes
        ApiKeysRecord apiKeyRecord = null;
        for (ApiKeysRecord candidate : candidates) {
            if (passwordEncoder.matches(apiKey, candidate.getKeyHash())) {
                apiKeyRecord = candidate;
                break;
            }
        }
        
        if (apiKeyRecord == null) {
            throw new BadRequestException("Invalid API key");
        }
        
        // Update last used timestamp
        apiKeyRepository.updateLastUsedAt(apiKeyRecord.getId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", apiKeyRecord.getId());
        result.put("bundleId", apiKeyRecord.getBundleId());
        result.put("organizationId", apiKeyRecord.getOrganizationId());
        result.put("name", apiKeyRecord.getName());
        result.put("createdBy", apiKeyRecord.getCreatedBy());
        
        return result;
    }
    
    /**
     * Generate a secure random API key
     */
    private String generateApiKey() {
        SecureRandom random = new SecureRandom();
        StringBuilder key = new StringBuilder(API_KEY_PREFIX);
        
        for (int i = 0; i < KEY_LENGTH; i++) {
            key.append(ALLOWED_CHARS.charAt(random.nextInt(ALLOWED_CHARS.length())));
        }
        
        return key.toString();
    }
    
    /**
     * Map API key record to response
     */
    private Map<String, Object> mapToApiKeyResponse(ApiKeysRecord record) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", record.getId());
        response.put("name", record.getName());
        response.put("keyPrefix", maskApiKey(record.getKeyPrefix()));
        response.put("bundleId", record.getBundleId());
        response.put("organizationId", record.getOrganizationId());
        response.put("createdBy", record.getCreatedBy());
        response.put("lastUsedAt", record.getLastUsedAt() != null 
                ? Date.from(record.getLastUsedAt().toInstant(ZoneOffset.UTC)) 
                : null);
        response.put("createdAt", Date.from(record.getCreatedAt().toInstant(ZoneOffset.UTC)));
        response.put("updatedAt", Date.from(record.getUpdatedAt().toInstant(ZoneOffset.UTC)));
        
        return response;
    }
    
    /**
     * Mask API key to show only first 4 and last 4 characters
     */
    private String maskApiKey(String keyPrefix) {
        if (keyPrefix == null || keyPrefix.length() <= 8) {
            return keyPrefix; // Too short to mask meaningfully
        }
        
        String first4 = keyPrefix.substring(0, 4);
        String last4 = keyPrefix.substring(keyPrefix.length() - 4);
        
        return first4 + "..." + last4;
    }
    
    /**
     * Verify that an app belongs to an organization
     */
    private void verifyAppBelongsToOrganization(String bundleId, String organizationId) {
        if (!mobileApplicationRepository.existsByBundleIdAndOrganizationId(bundleId, organizationId)) {
            throw new ResourceNotFoundException("Mobile application not found in this organization");
        }
    }
}

