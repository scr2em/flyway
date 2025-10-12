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
public class OrganizationRepository {
    
    private final DSLContext dsl;
    
    private static final String TABLE = "organizations";
    
    public Optional<Record> findById(String id) {
        return dsl.selectFrom(table(TABLE))
                .where(field("id").eq(id))
                .fetchOptional();
    }
    
    public List<Record> findAll() {
        return dsl.selectFrom(table(TABLE))
                .fetch();
    }
    
    public List<Record> findByCreatedBy(String createdBy) {
        return dsl.selectFrom(table(TABLE))
                .where(field("created_by").eq(createdBy))
                .fetch();
    }
    
    public String create(String name, String createdBy) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        dsl.insertInto(table(TABLE))
                .columns(
                        field("id"),
                        field("name"),
                        field("created_by"),
                        field("created_at"),
                        field("updated_at")
                )
                .values(id, name, createdBy, now, now)
                .execute();
        
        return id;
    }
    
    public int update(String id, String name) {
        return dsl.update(table(TABLE))
                .set(field("name"), name)
                .set(field("updated_at"), LocalDateTime.now())
                .where(field("id").eq(id))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(table(TABLE))
                .where(field("id").eq(id))
                .execute();
    }
}

