package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.AppBuildsRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.Flyway.server.jooq.tables.AppBuilds.APP_BUILDS;

@Repository
@RequiredArgsConstructor
public class AppBuildRepository {
    
    private final DSLContext dsl;
    
    /**
     * Find a build by organization, bundle ID, and commit hash
     */
    public Optional<AppBuildsRecord> findByKey(String organizationId, String bundleId, String commitHash) {
        return dsl.selectFrom(APP_BUILDS)
                .where(APP_BUILDS.ORGANIZATION_ID.eq(organizationId)
                        .and(APP_BUILDS.BUNDLE_ID.eq(bundleId))
                        .and(APP_BUILDS.COMMIT_HASH.eq(commitHash)))
                .fetchOptional();
    }
    
    /**
     * Find builds by organization ID with pagination and sorting
     */
    public List<AppBuildsRecord> findByOrganizationId(
            String organizationId,
            int limit,
            int offset,
            String sortDirection) {
        
        SortField<?> sortField = sortDirection.equalsIgnoreCase("asc") 
                ? APP_BUILDS.CREATED_AT.asc() 
                : APP_BUILDS.CREATED_AT.desc();
        
        return dsl.selectFrom(APP_BUILDS)
                .where(APP_BUILDS.ORGANIZATION_ID.eq(organizationId))
                .orderBy(sortField)
                .limit(limit)
                .offset(offset)
                .fetch();
    }
    
    /**
     * Find builds by bundle ID with pagination and sorting
     */
    public List<AppBuildsRecord> findByBundleId(
            String bundleId,
            int limit,
            int offset,
            String sortDirection) {
        
        SortField<?> sortField = sortDirection.equalsIgnoreCase("asc") 
                ? APP_BUILDS.CREATED_AT.asc() 
                : APP_BUILDS.CREATED_AT.desc();
        
        return dsl.selectFrom(APP_BUILDS)
                .where(APP_BUILDS.BUNDLE_ID.eq(bundleId))
                .orderBy(sortField)
                .limit(limit)
                .offset(offset)
                .fetch();
    }
    
    /**
     * Find builds by organization and bundle ID with pagination and sorting
     */
    public List<AppBuildsRecord> findByOrganizationAndBundleId(
            String organizationId,
            String bundleId,
            int limit,
            int offset,
            String sortDirection) {
        
        SortField<?> sortField = sortDirection.equalsIgnoreCase("asc") 
                ? APP_BUILDS.CREATED_AT.asc() 
                : APP_BUILDS.CREATED_AT.desc();
        
        return dsl.selectFrom(APP_BUILDS)
                .where(APP_BUILDS.ORGANIZATION_ID.eq(organizationId)
                        .and(APP_BUILDS.BUNDLE_ID.eq(bundleId)))
                .orderBy(sortField)
                .limit(limit)
                .offset(offset)
                .fetch();
    }
    
    /**
     * Count builds by organization ID
     */
    public int countByOrganizationId(String organizationId) {
        return dsl.fetchCount(
                dsl.selectFrom(APP_BUILDS)
                        .where(APP_BUILDS.ORGANIZATION_ID.eq(organizationId))
        );
    }
    
    /**
     * Count builds by bundle ID
     */
    public int countByBundleId(String bundleId) {
        return dsl.fetchCount(
                dsl.selectFrom(APP_BUILDS)
                        .where(APP_BUILDS.BUNDLE_ID.eq(bundleId))
        );
    }
    
    /**
     * Count builds by organization and bundle ID
     */
    public int countByOrganizationAndBundleId(String organizationId, String bundleId) {
        return dsl.fetchCount(
                dsl.selectFrom(APP_BUILDS)
                        .where(APP_BUILDS.ORGANIZATION_ID.eq(organizationId)
                                .and(APP_BUILDS.BUNDLE_ID.eq(bundleId)))
        );
    }
    
    /**
     * Create a new build
     */
    public void create(
            String organizationId,
            String bundleId,
            String commitHash,
            String branchName,
            String commitMessage,
            Long buildSize,
            String buildUrl,
            String nativeVersion,
            String uploadedBy) {
        
        LocalDateTime now = LocalDateTime.now();
        
        AppBuildsRecord record = dsl.newRecord(APP_BUILDS);
        record.setOrganizationId(organizationId);
        record.setBundleId(bundleId);
        record.setCommitHash(commitHash);
        record.setBranchName(branchName);
        record.setCommitMessage(commitMessage);
        record.setBuildSize(buildSize);
        record.setBuildUrl(buildUrl);
        record.setNativeVersion(nativeVersion);
        record.setUploadedBy(uploadedBy);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.store();
    }
    
    /**
     * Delete a build by its composite key
     */
    public int delete(String organizationId, String bundleId, String commitHash) {
        return dsl.deleteFrom(APP_BUILDS)
                .where(APP_BUILDS.ORGANIZATION_ID.eq(organizationId)
                        .and(APP_BUILDS.BUNDLE_ID.eq(bundleId))
                        .and(APP_BUILDS.COMMIT_HASH.eq(commitHash)))
                .execute();
    }
    
    /**
     * Check if a build exists
     */
    public boolean exists(String organizationId, String bundleId, String commitHash) {
        return dsl.fetchExists(
                dsl.selectFrom(APP_BUILDS)
                        .where(APP_BUILDS.ORGANIZATION_ID.eq(organizationId)
                                .and(APP_BUILDS.BUNDLE_ID.eq(bundleId))
                                .and(APP_BUILDS.COMMIT_HASH.eq(commitHash)))
        );
    }
}

