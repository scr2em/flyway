package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.RolesRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.Flyway.server.jooq.tables.Roles.ROLES;

@Repository
@RequiredArgsConstructor
public class RoleRepository {
    
    private final DSLContext dsl;
    
    public Optional<RolesRecord> findById(String id) {
        return dsl.selectFrom(ROLES)
                .where(ROLES.ID.eq(id))
                .fetchOptional();
    }
    
    public List<RolesRecord> findAll() {
        return dsl.selectFrom(ROLES)
                .fetch();
    }
    
    public Optional<RolesRecord> findByName(String name) {
        return dsl.selectFrom(ROLES)
                .where(ROLES.NAME.eq(name))
                .fetchOptional();
    }
    
    public String create(String name, String description, long permissions) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        RolesRecord record = dsl.newRecord(ROLES);
        record.setId(id);
        record.setName(name);
        record.setDescription(description);
        record.setPermissions(permissions);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.store();
        
        return id;
    }
    
    public int update(String id, String name, String description, Long permissions) {
        var updateStep = dsl.update(ROLES);
        var setStep = updateStep.set(ROLES.UPDATED_AT, LocalDateTime.now());
        
        if (name != null) {
            setStep = setStep.set(ROLES.NAME, name);
        }
        
        if (description != null) {
            setStep = setStep.set(ROLES.DESCRIPTION, description);
        }
        
        if (permissions != null) {
            setStep = setStep.set(ROLES.PERMISSIONS, permissions);
        }
        
        return setStep
                .where(ROLES.ID.eq(id))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(ROLES)
                .where(ROLES.ID.eq(id))
                .execute();
    }
}

