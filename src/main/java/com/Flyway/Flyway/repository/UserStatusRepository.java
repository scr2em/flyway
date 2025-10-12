package com.Flyway.Flyway.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.*;

@Repository
@RequiredArgsConstructor
public class UserStatusRepository {
    
    private final DSLContext dsl;
    
    private static final String TABLE = "user_statuses";
    
    public Optional<Record> findById(String id) {
        return dsl.selectFrom(table(TABLE))
                .where(field("id").eq(id))
                .fetchOptional();
    }
    
    public Optional<Record> findByCode(String code) {
        return dsl.selectFrom(table(TABLE))
                .where(field("code").eq(code))
                .fetchOptional();
    }
    
    public List<Record> findAll() {
        return dsl.selectFrom(table(TABLE))
                .fetch();
    }
}

