package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.OrganizationMembersRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.Flyway.server.jooq.tables.OrganizationMembers.ORGANIZATION_MEMBERS;

@Repository
@RequiredArgsConstructor
public class OrganizationMemberRepository {
    
    private final DSLContext dsl;
    
    public Optional<OrganizationMembersRecord> findById(String id) {
        return dsl.selectFrom(ORGANIZATION_MEMBERS)
                .where(ORGANIZATION_MEMBERS.ID.eq(id))
                .fetchOptional();
    }
    
    public List<OrganizationMembersRecord> findByOrganizationId(String organizationId) {
        return dsl.selectFrom(ORGANIZATION_MEMBERS)
                .where(ORGANIZATION_MEMBERS.ORGANIZATION_ID.eq(organizationId))
                .fetch();
    }
    
    public List<OrganizationMembersRecord> findByOrganizationIdWithPagination(
            String organizationId, int limit, int offset) {
        return dsl.selectFrom(ORGANIZATION_MEMBERS)
                .where(ORGANIZATION_MEMBERS.ORGANIZATION_ID.eq(organizationId))
                .orderBy(ORGANIZATION_MEMBERS.JOINED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch();
    }
    
    public int countByOrganizationId(String organizationId) {
        return dsl.fetchCount(
                dsl.selectFrom(ORGANIZATION_MEMBERS)
                        .where(ORGANIZATION_MEMBERS.ORGANIZATION_ID.eq(organizationId))
        );
    }
    
    public List<OrganizationMembersRecord> findByUserId(String userId) {
        return dsl.selectFrom(ORGANIZATION_MEMBERS)
                .where(ORGANIZATION_MEMBERS.USER_ID.eq(userId))
                .fetch();
    }
    
    public List<OrganizationMembersRecord> findByRoleId(String roleId) {
        return dsl.selectFrom(ORGANIZATION_MEMBERS)
                .where(ORGANIZATION_MEMBERS.ROLE_ID.eq(roleId))
                .fetch();
    }
    
    public Optional<OrganizationMembersRecord> findByOrganizationIdAndUserId(String organizationId, String userId) {
        return dsl.selectFrom(ORGANIZATION_MEMBERS)
                .where(ORGANIZATION_MEMBERS.ORGANIZATION_ID.eq(organizationId)
                        .and(ORGANIZATION_MEMBERS.USER_ID.eq(userId)))
                .fetchOptional();
    }
    
    public String create(String organizationId, String userId, String roleId) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        OrganizationMembersRecord record = dsl.newRecord(ORGANIZATION_MEMBERS);
        record.setId(id);
        record.setOrganizationId(organizationId);
        record.setUserId(userId);
        record.setRoleId(roleId);
        record.setJoinedAt(now);
        record.setCreatedAt(now);
        record.store();
        
        return id;
    }
    
    public int updateRole(String id, String roleId) {
        return dsl.update(ORGANIZATION_MEMBERS)
                .set(ORGANIZATION_MEMBERS.ROLE_ID, roleId)
                .where(ORGANIZATION_MEMBERS.ID.eq(id))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(ORGANIZATION_MEMBERS)
                .where(ORGANIZATION_MEMBERS.ID.eq(id))
                .execute();
    }
    
    public boolean existsByOrganizationIdAndUserId(String organizationId, String userId) {
        return dsl.fetchExists(
                dsl.selectFrom(ORGANIZATION_MEMBERS)
                        .where(ORGANIZATION_MEMBERS.ORGANIZATION_ID.eq(organizationId)
                                .and(ORGANIZATION_MEMBERS.USER_ID.eq(userId)))
        );
    }
}

