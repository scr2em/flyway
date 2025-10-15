package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.ApiKeysRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.Flyway.server.jooq.tables.ApiKeys.API_KEYS;

@Repository
@RequiredArgsConstructor
public class ApiKeyRepository {
    
    private final DSLContext dsl;
    
    /**
     * Find an API key by ID
     */
    public Optional<ApiKeysRecord> findById(String id) {
        return dsl.selectFrom(API_KEYS)
                .where(API_KEYS.ID.eq(id))
                .fetchOptional();
    }
    
    /**
     * Find an API key by its hash
     */
    public Optional<ApiKeysRecord> findByKeyHash(String keyHash) {
        return dsl.selectFrom(API_KEYS)
                .where(API_KEYS.KEY_HASH.eq(keyHash))
                .fetchOptional();
    }
    
    /**
     * Find all API keys for a specific bundle ID
     */
    public List<ApiKeysRecord> findByBundleId(String bundleId) {
        return dsl.selectFrom(API_KEYS)
                .where(API_KEYS.BUNDLE_ID.eq(bundleId))
                .orderBy(API_KEYS.CREATED_AT.desc())
                .fetch();
    }
    
    /**
     * Find all API keys for an organization
     */
    public List<ApiKeysRecord> findByOrganizationId(String organizationId) {
        return dsl.selectFrom(API_KEYS)
                .where(API_KEYS.ORGANIZATION_ID.eq(organizationId))
                .orderBy(API_KEYS.CREATED_AT.desc())
                .fetch();
    }
    
    /**
     * Find all API keys for a bundle ID and organization
     */
    public List<ApiKeysRecord> findByBundleIdAndOrganizationId(String bundleId, String organizationId) {
        return dsl.selectFrom(API_KEYS)
                .where(API_KEYS.BUNDLE_ID.eq(bundleId)
                        .and(API_KEYS.ORGANIZATION_ID.eq(organizationId)))
                .orderBy(API_KEYS.CREATED_AT.desc())
                .fetch();
    }
    
    /**
     * Create a new API key
     */
    public ApiKeysRecord create(
            String id,
            String keyHash,
            String keyPrefix,
            String name,
            String bundleId,
            String organizationId,
            String createdBy,
            LocalDateTime expiresAt) {
        
        LocalDateTime now = LocalDateTime.now();
        
        ApiKeysRecord record = dsl.newRecord(API_KEYS);
        record.setId(id);
        record.setKeyHash(keyHash);
        record.setKeyPrefix(keyPrefix);
        record.setName(name);
        record.setBundleId(bundleId);
        record.setOrganizationId(organizationId);
        record.setCreatedBy(createdBy);
        record.setExpiresAt(expiresAt);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.store();
        
        return record;
    }
    
    /**
     * Update the last used timestamp
     */
    public void updateLastUsedAt(String id) {
        dsl.update(API_KEYS)
                .set(API_KEYS.LAST_USED_AT, LocalDateTime.now())
                .where(API_KEYS.ID.eq(id))
                .execute();
    }
    
    /**
     * Delete an API key by ID
     */
    public int delete(String id) {
        return dsl.deleteFrom(API_KEYS)
                .where(API_KEYS.ID.eq(id))
                .execute();
    }
    
    /**
     * Check if an API key exists by ID
     */
    public boolean existsById(String id) {
        return dsl.fetchExists(
                dsl.selectFrom(API_KEYS)
                        .where(API_KEYS.ID.eq(id))
        );
    }
    
    /**
     * Check if a key prefix already exists (to ensure uniqueness)
     */
    public boolean existsByKeyPrefix(String keyPrefix) {
        return dsl.fetchExists(
                dsl.selectFrom(API_KEYS)
                        .where(API_KEYS.KEY_PREFIX.eq(keyPrefix))
        );
    }
    
    /**
     * Find API key by prefix (for validation)
     */
    public List<ApiKeysRecord> findByKeyPrefix(String keyPrefix) {
        return dsl.selectFrom(API_KEYS)
                .where(API_KEYS.KEY_PREFIX.eq(keyPrefix))
                .fetch();
    }
}

