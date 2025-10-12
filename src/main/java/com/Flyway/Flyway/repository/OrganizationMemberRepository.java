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
public class OrganizationMemberRepository {
    
    private final DSLContext dsl;
    
    private static final String TABLE = "organization_members";
    
    public Optional<Record> findById(String id) {
        return dsl.selectFrom(table(TABLE))
                .where(field("id").eq(id))
                .fetchOptional();
    }
    
    public List<Record> findByOrganizationId(String organizationId) {
        return dsl.selectFrom(table(TABLE))
                .where(field("organization_id").eq(organizationId))
                .fetch();
    }
    
    public List<Record> findByUserId(String userId) {
        return dsl.selectFrom(table(TABLE))
                .where(field("user_id").eq(userId))
                .fetch();
    }
    
    public Optional<Record> findByOrganizationIdAndUserId(String organizationId, String userId) {
        return dsl.selectFrom(table(TABLE))
                .where(field("organization_id").eq(organizationId)
                        .and(field("user_id").eq(userId)))
                .fetchOptional();
    }
    
    public String create(String organizationId, String userId, String roleId) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        dsl.insertInto(table(TABLE))
                .columns(
                        field("id"),
                        field("organization_id"),
                        field("user_id"),
                        field("role_id"),
                        field("joined_at"),
                        field("created_at")
                )
                .values(id, organizationId, userId, roleId, now, now)
                .execute();
        
        return id;
    }
    
    public int updateRole(String id, String roleId) {
        return dsl.update(table(TABLE))
                .set(field("role_id"), roleId)
                .where(field("id").eq(id))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(table(TABLE))
                .where(field("id").eq(id))
                .execute();
    }
    
    public boolean existsByOrganizationIdAndUserId(String organizationId, String userId) {
        return dsl.fetchExists(
                dsl.selectFrom(table(TABLE))
                        .where(field("organization_id").eq(organizationId)
                                .and(field("user_id").eq(userId)))
        );
    }
}

