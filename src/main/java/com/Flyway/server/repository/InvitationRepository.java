package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.InvitationsRecord;
import com.Flyway.server.jooq.tables.records.InvitationStatusesRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.Flyway.server.jooq.tables.Invitations.INVITATIONS;
import static com.Flyway.server.jooq.tables.InvitationStatuses.INVITATION_STATUSES;

@Repository
@RequiredArgsConstructor
public class InvitationRepository {
    
    private final DSLContext dsl;
    
    public Optional<InvitationsRecord> findById(String id) {
        return dsl.selectFrom(INVITATIONS)
                .where(INVITATIONS.ID.eq(id))
                .fetchOptional();
    }
    
    public Optional<InvitationsRecord> findByToken(String token) {
        return dsl.selectFrom(INVITATIONS)
                .where(INVITATIONS.TOKEN.eq(token))
                .fetchOptional();
    }
    
    public List<InvitationsRecord> findByOrganizationId(String organizationId) {
        return dsl.selectFrom(INVITATIONS)
                .where(INVITATIONS.ORGANIZATION_ID.eq(organizationId))
                .fetch();
    }
    
    public List<InvitationsRecord> findByEmail(String email) {
        return dsl.selectFrom(INVITATIONS)
                .where(INVITATIONS.EMAIL.eq(email))
                .fetch();
    }
    
    public String create(String organizationId, String email, String roleId, String invitedBy, 
                        String invitationStatusId, String token, LocalDateTime expiresAt) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        InvitationsRecord record = dsl.newRecord(INVITATIONS);
        record.setId(id);
        record.setOrganizationId(organizationId);
        record.setEmail(email);
        record.setRoleId(roleId);
        record.setInvitedBy(invitedBy);
        record.setInvitationStatusId(invitationStatusId);
        record.setToken(token);
        record.setExpiresAt(expiresAt);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.store();
        
        return id;
    }
    
    public int updateStatus(String id, String statusId) {
        return dsl.update(INVITATIONS)
                .set(INVITATIONS.INVITATION_STATUS_ID, statusId)
                .set(INVITATIONS.RESPONDED_AT, LocalDateTime.now())
                .set(INVITATIONS.UPDATED_AT, LocalDateTime.now())
                .where(INVITATIONS.ID.eq(id))
                .execute();
    }
    
    public int updateToken(String id, String token) {
        return dsl.update(INVITATIONS)
                .set(INVITATIONS.TOKEN, token)
                .set(INVITATIONS.UPDATED_AT, LocalDateTime.now())
                .where(INVITATIONS.ID.eq(id))
                .execute();
    }
    
    public int updateExpiresAt(String id, LocalDateTime expiresAt) {
        return dsl.update(INVITATIONS)
                .set(INVITATIONS.EXPIRES_AT, expiresAt)
                .set(INVITATIONS.UPDATED_AT, LocalDateTime.now())
                .where(INVITATIONS.ID.eq(id))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(INVITATIONS)
                .where(INVITATIONS.ID.eq(id))
                .execute();
    }
    
    public int deleteByEmailAndOrganizationId(String email, String organizationId) {
        return dsl.deleteFrom(INVITATIONS)
                .where(INVITATIONS.EMAIL.eq(email)
                        .and(INVITATIONS.ORGANIZATION_ID.eq(organizationId)))
                .execute();
    }
    
    public int markExpired() {
        // Get pending status ID first
        Optional<InvitationStatusesRecord> pendingStatus = dsl.selectFrom(INVITATION_STATUSES)
                .where(INVITATION_STATUSES.CODE.eq("pending"))
                .fetchOptional();
        
        Optional<InvitationStatusesRecord> expiredStatus = dsl.selectFrom(INVITATION_STATUSES)
                .where(INVITATION_STATUSES.CODE.eq("expired"))
                .fetchOptional();
        
        if (pendingStatus.isEmpty() || expiredStatus.isEmpty()) {
            return 0;
        }
        
        return dsl.update(INVITATIONS)
                .set(INVITATIONS.INVITATION_STATUS_ID, expiredStatus.get().getId())
                .set(INVITATIONS.UPDATED_AT, LocalDateTime.now())
                .where(INVITATIONS.EXPIRES_AT.lt(LocalDateTime.now())
                        .and(INVITATIONS.INVITATION_STATUS_ID.eq(pendingStatus.get().getId())))
                .execute();
    }
}

