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
    
    public List<RolesRecord> findByOrganizationId(String organizationId) {
        return dsl.selectFrom(ROLES)
                .where(ROLES.ORGANIZATION_ID.eq(organizationId))
                .fetch();
    }
    
    public Optional<RolesRecord> findByOrganizationIdAndName(String organizationId, String name) {
        return dsl.selectFrom(ROLES)
                .where(ROLES.ORGANIZATION_ID.eq(organizationId)
                        .and(ROLES.NAME.eq(name)))
                .fetchOptional();
    }
    
    public String create(String organizationId, String name, boolean isSystemRole, boolean isImmutable) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        RolesRecord record = dsl.newRecord(ROLES);
        record.setId(id);
        record.setOrganizationId(organizationId);
        record.setName(name);
        record.setIsSystemRole(isSystemRole ? (byte) 1 : (byte) 0);
        record.setIsImmutable(isImmutable ? (byte) 1 : (byte) 0);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.store();
        
        return id;
    }
    
    public int update(String id, String name) {
        return dsl.update(ROLES)
                .set(ROLES.NAME, name)
                .set(ROLES.UPDATED_AT, LocalDateTime.now())
                .where(ROLES.ID.eq(id)
                        .and(ROLES.IS_IMMUTABLE.eq((byte) 0)))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(ROLES)
                .where(ROLES.ID.eq(id)
                        .and(ROLES.IS_IMMUTABLE.eq((byte) 0)))
                .execute();
    }
}

