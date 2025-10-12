package com.Flyway.Flyway.repository;

import com.Flyway.Flyway.jooq.tables.records.OrganizationsRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.Flyway.Flyway.jooq.tables.Organizations.ORGANIZATIONS;

@Repository
@RequiredArgsConstructor
public class OrganizationRepository {
    
    private final DSLContext dsl;
    
    public Optional<OrganizationsRecord> findById(String id) {
        return dsl.selectFrom(ORGANIZATIONS)
                .where(ORGANIZATIONS.ID.eq(id))
                .fetchOptional();
    }
    
    public List<OrganizationsRecord> findAll() {
        return dsl.selectFrom(ORGANIZATIONS)
                .fetch();
    }
    
    public List<OrganizationsRecord> findByCreatedBy(String createdBy) {
        return dsl.selectFrom(ORGANIZATIONS)
                .where(ORGANIZATIONS.CREATED_BY.eq(createdBy))
                .fetch();
    }
    
    public String create(String name, String createdBy) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        OrganizationsRecord record = dsl.newRecord(ORGANIZATIONS);
        record.setId(id);
        record.setName(name);
        record.setCreatedBy(createdBy);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.store();
        
        return id;
    }
    
    public int update(String id, String name) {
        return dsl.update(ORGANIZATIONS)
                .set(ORGANIZATIONS.NAME, name)
                .set(ORGANIZATIONS.UPDATED_AT, LocalDateTime.now())
                .where(ORGANIZATIONS.ID.eq(id))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(ORGANIZATIONS)
                .where(ORGANIZATIONS.ID.eq(id))
                .execute();
    }
}

