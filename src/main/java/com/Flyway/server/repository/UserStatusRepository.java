package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.UserStatusesRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.Flyway.server.jooq.tables.UserStatuses.USER_STATUSES;

@Repository
@RequiredArgsConstructor
public class UserStatusRepository {
    
    private final DSLContext dsl;
    
    public Optional<UserStatusesRecord> findById(String id) {
        return dsl.selectFrom(USER_STATUSES)
                .where(USER_STATUSES.ID.eq(id))
                .fetchOptional();
    }
    
    public Optional<UserStatusesRecord> findByCode(String code) {
        return dsl.selectFrom(USER_STATUSES)
                .where(USER_STATUSES.CODE.eq(code))
                .fetchOptional();
    }
    
    public List<UserStatusesRecord> findAll() {
        return dsl.selectFrom(USER_STATUSES)
                .fetch();
    }
}

