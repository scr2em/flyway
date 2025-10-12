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
public class InvitationRepository {
    
    private final DSLContext dsl;
    
    private static final String TABLE = "invitations";
    
    public Optional<Record> findById(String id) {
        return dsl.selectFrom(table(TABLE))
                .where(field("id").eq(id))
                .fetchOptional();
    }
    
    public Optional<Record> findByToken(String token) {
        return dsl.selectFrom(table(TABLE))
                .where(field("token").eq(token))
                .fetchOptional();
    }
    
    public List<Record> findByOrganizationId(String organizationId) {
        return dsl.selectFrom(table(TABLE))
                .where(field("organization_id").eq(organizationId))
                .fetch();
    }
    
    public List<Record> findByEmail(String email) {
        return dsl.selectFrom(table(TABLE))
                .where(field("email").eq(email))
                .fetch();
    }
    
    public String create(String organizationId, String email, String roleId, String invitedBy, 
                        String invitationStatusId, String token, LocalDateTime expiresAt) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        dsl.insertInto(table(TABLE))
                .columns(
                        field("id"),
                        field("organization_id"),
                        field("email"),
                        field("role_id"),
                        field("invited_by"),
                        field("invitation_status_id"),
                        field("token"),
                        field("expires_at"),
                        field("created_at"),
                        field("updated_at")
                )
                .values(id, organizationId, email, roleId, invitedBy, invitationStatusId, token, expiresAt, now, now)
                .execute();
        
        return id;
    }
    
    public int updateStatus(String id, String statusId) {
        return dsl.update(table(TABLE))
                .set(field("invitation_status_id"), statusId)
                .set(field("responded_at"), LocalDateTime.now())
                .set(field("updated_at"), LocalDateTime.now())
                .where(field("id").eq(id))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(table(TABLE))
                .where(field("id").eq(id))
                .execute();
    }
    
    public int markExpired() {
        // Get pending status ID first
        Optional<Record> pendingStatus = dsl.selectFrom(table("invitation_statuses"))
                .where(field("code").eq("pending"))
                .fetchOptional();
        
        Optional<Record> expiredStatus = dsl.selectFrom(table("invitation_statuses"))
                .where(field("code").eq("expired"))
                .fetchOptional();
        
        if (pendingStatus.isEmpty() || expiredStatus.isEmpty()) {
            return 0;
        }
        
        return dsl.update(table(TABLE))
                .set(field("invitation_status_id"), expiredStatus.get().get("id"))
                .set(field("updated_at"), LocalDateTime.now())
                .where(field("expires_at").lt(LocalDateTime.now())
                        .and(field("invitation_status_id").eq(pendingStatus.get().get("id"))))
                .execute();
    }
}

