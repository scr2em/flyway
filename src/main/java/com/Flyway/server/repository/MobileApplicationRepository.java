package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.MobileApplicationsRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.Flyway.server.jooq.tables.MobileApplications.MOBILE_APPLICATIONS;

@Repository
@RequiredArgsConstructor
public class MobileApplicationRepository {
    
    private final DSLContext dsl;
    
    public Optional<MobileApplicationsRecord> findById(String id) {
        return dsl.selectFrom(MOBILE_APPLICATIONS)
                .where(MOBILE_APPLICATIONS.ID.eq(id))
                .fetchOptional();
    }
    
    public Optional<MobileApplicationsRecord> findByBundleId(String bundleId) {
        return dsl.selectFrom(MOBILE_APPLICATIONS)
                .where(MOBILE_APPLICATIONS.BUNDLE_ID.eq(bundleId))
                .fetchOptional();
    }
    
    public List<MobileApplicationsRecord> findByOrganizationId(String organizationId) {
        return dsl.selectFrom(MOBILE_APPLICATIONS)
                .where(MOBILE_APPLICATIONS.ORGANIZATION_ID.eq(organizationId))
                .fetch();
    }
    
    public List<MobileApplicationsRecord> findAll() {
        return dsl.selectFrom(MOBILE_APPLICATIONS)
                .fetch();
    }
    
    public String create(String bundleId, String organizationId, String name, String description, String createdBy) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        MobileApplicationsRecord record = dsl.newRecord(MOBILE_APPLICATIONS);
        record.setId(id);
        record.setBundleId(bundleId);
        record.setOrganizationId(organizationId);
        record.setName(name);
        record.setDescription(description);
        record.setCreatedBy(createdBy);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.store();
        
        return id;
    }
    
    public int update(String id, String name, String description) {
        return dsl.update(MOBILE_APPLICATIONS)
                .set(MOBILE_APPLICATIONS.NAME, name)
                .set(MOBILE_APPLICATIONS.DESCRIPTION, description)
                .set(MOBILE_APPLICATIONS.UPDATED_AT, LocalDateTime.now())
                .where(MOBILE_APPLICATIONS.ID.eq(id))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(MOBILE_APPLICATIONS)
                .where(MOBILE_APPLICATIONS.ID.eq(id))
                .execute();
    }
    
    public boolean existsByBundleId(String bundleId) {
        return dsl.fetchExists(
                dsl.selectFrom(MOBILE_APPLICATIONS)
                        .where(MOBILE_APPLICATIONS.BUNDLE_ID.eq(bundleId))
        );
    }
    
    public boolean existsByNameAndOrganizationId(String name, String organizationId) {
        return dsl.fetchExists(
                dsl.selectFrom(MOBILE_APPLICATIONS)
                        .where(MOBILE_APPLICATIONS.NAME.eq(name)
                                .and(MOBILE_APPLICATIONS.ORGANIZATION_ID.eq(organizationId)))
        );
    }
    
    public boolean belongsToOrganization(String appId, String organizationId) {
        return dsl.fetchExists(
                dsl.selectFrom(MOBILE_APPLICATIONS)
                        .where(MOBILE_APPLICATIONS.ID.eq(appId)
                                .and(MOBILE_APPLICATIONS.ORGANIZATION_ID.eq(organizationId)))
        );
    }
    
    public boolean existsByBundleIdAndOrganizationId(String bundleId, String organizationId) {
        return dsl.fetchExists(
                dsl.selectFrom(MOBILE_APPLICATIONS)
                        .where(MOBILE_APPLICATIONS.BUNDLE_ID.eq(bundleId)
                                .and(MOBILE_APPLICATIONS.ORGANIZATION_ID.eq(organizationId)))
        );
    }
}

