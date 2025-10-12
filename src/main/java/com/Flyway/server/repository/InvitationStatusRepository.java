package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.InvitationStatusesRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.Flyway.server.jooq.tables.InvitationStatuses.INVITATION_STATUSES;

@Repository
@RequiredArgsConstructor
public class InvitationStatusRepository {
    
    private final DSLContext dsl;
    
    public Optional<InvitationStatusesRecord> findById(String id) {
        return dsl.selectFrom(INVITATION_STATUSES)
                .where(INVITATION_STATUSES.ID.eq(id))
                .fetchOptional();
    }
    
    public Optional<InvitationStatusesRecord> findByCode(String code) {
        return dsl.selectFrom(INVITATION_STATUSES)
                .where(INVITATION_STATUSES.CODE.eq(code))
                .fetchOptional();
    }
    
    public List<InvitationStatusesRecord> findAll() {
        return dsl.selectFrom(INVITATION_STATUSES)
                .fetch();
    }
}

