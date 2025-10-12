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
public class UserRepository {
    
    private final DSLContext dsl;
    
    private static final String TABLE = "users";
    
    public Optional<Record> findById(String id) {
        return dsl.selectFrom(table(TABLE))
                .where(field("id").eq(id))
                .fetchOptional();
    }
    
    public Optional<Record> findByEmail(String email) {
        return dsl.selectFrom(table(TABLE))
                .where(field("email").eq(email))
                .fetchOptional();
    }
    
    public List<Record> findAll() {
        return dsl.selectFrom(table(TABLE))
                .fetch();
    }
    
    public String create(String firstName, String lastName, String email, String passwordHash, String userStatusId) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        dsl.insertInto(table(TABLE))
                .columns(
                        field("id"),
                        field("first_name"),
                        field("last_name"),
                        field("email"),
                        field("password_hash"),
                        field("user_status_id"),
                        field("email_verified"),
                        field("created_at"),
                        field("updated_at")
                )
                .values(id, firstName, lastName, email, passwordHash, userStatusId, false, now, now)
                .execute();
        
        return id;
    }
    
    public int update(String id, String firstName, String lastName) {
        return dsl.update(table(TABLE))
                .set(field("first_name"), firstName)
                .set(field("last_name"), lastName)
                .set(field("updated_at"), LocalDateTime.now())
                .where(field("id").eq(id))
                .execute();
    }
    
    public int updateLastLogin(String id) {
        return dsl.update(table(TABLE))
                .set(field("last_login_at"), LocalDateTime.now())
                .where(field("id").eq(id))
                .execute();
    }
    
    public int verifyEmail(String id) {
        LocalDateTime now = LocalDateTime.now();
        return dsl.update(table(TABLE))
                .set(field("email_verified"), true)
                .set(field("email_verified_at"), now)
                .set(field("updated_at"), now)
                .where(field("id").eq(id))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(table(TABLE))
                .where(field("id").eq(id))
                .execute();
    }
    
    public boolean existsByEmail(String email) {
        return dsl.fetchExists(
                dsl.selectFrom(table(TABLE))
                        .where(field("email").eq(email))
        );
    }
}

