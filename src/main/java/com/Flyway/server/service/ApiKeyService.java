package com.Flyway.server.service;

import com.Flyway.server.dto.generated.ApiKeyResponse;
import com.Flyway.server.dto.generated.PaginatedApiKeyResponse;
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
    public ApiKeyResponse createApiKey(
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
        ApiKeyResponse response = mapToApiKeyResponse(record);
        response.key(apiKey); // Include the plain key only on creation
        
        return response;

    }
    
 
    
    /**
     * Get API keys with pagination and sorting
     */
    public PaginatedApiKeyResponse getApiKeysByBundleIdPaginated(
            String bundleId,
            String organizationId,
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
        
        List<ApiKeysRecord> apiKeys = apiKeyRepository.findByBundleIdAndOrganizationId(
                bundleId, organizationId, size, offset, sort);
        int totalCount = apiKeyRepository.countByBundleIdAndOrganizationId(bundleId, organizationId);
        
        List<ApiKeyResponse> apiKeyResponses = apiKeys.stream()
                .map(this::mapToApiKeyResponse)
                .collect(Collectors.toList());
        
    
        
        return new PaginatedApiKeyResponse()
                .data(apiKeyResponses)
                .page(page)
                .size(size)
                .totalElements(totalCount)
                .totalPages((int) Math.ceil((double) totalCount / size));
    }
    
    @Transactional
    public void deleteApiKey(String id) {
        
        if (apiKeyRepository.delete(id) == 0) {
            throw new ResourceNotFoundException("API key not found");
        }

    }
    
    /**
     * Direct lookup of API key by exact value
     */
    public ApiKeyResponse lookupApiKey(String apiKey) {
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
        
        return mapToApiKeyResponse(apiKeyRecord);
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
    private ApiKeyResponse mapToApiKeyResponse(ApiKeysRecord record) {
      
        return new ApiKeyResponse().id(record.getId())
                .name(record.getName())
                .keyPrefix(maskApiKey(record.getKeyPrefix()))
                .bundleId(record.getBundleId())
                .organizationId(record.getOrganizationId())
                .createdBy(record.getCreatedBy())
                .lastUsedAt(record.getLastUsedAt() != null ? record.getLastUsedAt().atOffset(ZoneOffset.UTC) : null)
                .createdAt(record.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(record.getUpdatedAt().atOffset(ZoneOffset.UTC));
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

