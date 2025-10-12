package com.Flyway.Flyway.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
@RequiredArgsConstructor
public class RoleRepository {
    
    private final DSLContext dsl;
    
    private static final String TABLE = "roles";
    
    public Optional<Record> findById(String id) {
        return dsl.selectFrom(table(TABLE))
                .where(field("id").eq(id))
                .fetchOptional();
    }
    
    public List<Record> findAll() {
        return dsl.selectFrom(table(TABLE))
                .fetch();
    }
    
    public List<Record> findByOrganizationId(String organizationId) {
        return dsl.selectFrom(table(TABLE))
                .where(field("organization_id").eq(organizationId))
                .fetch();
    }
    
    public Optional<Record> findByOrganizationIdAndName(String organizationId, String name) {
        return dsl.selectFrom(table(TABLE))
                .where(field("organization_id").eq(organizationId))
                .where(field("name").eq(name))
                .fetchOptional();
    }
    
    public String create(String organizationId, String name, boolean isSystemRole, boolean isImmutable) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        dsl.insertInto(table(TABLE))
                .columns(
                        field("id"),
                        field("organization_id"),
                        field("name"),
                        field("is_system_role"),
                        field("is_immutable"),
                        field("created_at"),
                        field("updated_at")
                )
                .values(id, organizationId, name, isSystemRole, isImmutable, now, now)
                .execute();
        
        return id;
    }
    
    public int update(String id, String name) {
        return dsl.update(table(TABLE))
                .set(field("name"), name)
                .set(field("updated_at"), LocalDateTime.now())
                .where(field("id").eq(id))
                .where(field("is_immutable").eq(false))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(table(TABLE))
                .where(field("id").eq(id))
                .where(field("is_immutable").eq(false))
                .execute();
    }
}

