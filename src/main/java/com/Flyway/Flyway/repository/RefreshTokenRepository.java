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
public class RefreshTokenRepository {
    
    private final DSLContext dsl;
    
    private static final String TABLE = "refresh_tokens";
    
    public Optional<Record> findById(String id) {
        return dsl.selectFrom(table(TABLE))
                .where(field("id").eq(id))
                .fetchOptional();
    }
    
    public Optional<Record> findByTokenHash(String tokenHash) {
        return dsl.selectFrom(table(TABLE))
                .where(field("token_hash").eq(tokenHash))
                .where(field("is_revoked").eq(false))
                .fetchOptional();
    }
    
    public List<Record> findByUserId(String userId) {
        return dsl.selectFrom(table(TABLE))
                .where(field("user_id").eq(userId))
                .fetch();
    }
    
    public String create(String userId, String tokenHash, LocalDateTime expiresAt, String deviceInfo, String ipAddress) {
        String id = UUID.randomUUID().toString();
        
        dsl.insertInto(table(TABLE))
                .columns(
                        field("id"),
                        field("user_id"),
                        field("token_hash"),
                        field("is_revoked"),
                        field("device_info"),
                        field("ip_address"),
                        field("expires_at"),
                        field("created_at")
                )
                .values(id, userId, tokenHash, false, deviceInfo, ipAddress, expiresAt, LocalDateTime.now())
                .execute();
        
        return id;
    }
    
    public int revoke(String id) {
        return dsl.update(table(TABLE))
                .set(field("is_revoked"), true)
                .set(field("revoked_at"), LocalDateTime.now())
                .where(field("id").eq(id))
                .execute();
    }
    
    public int revokeByTokenHash(String tokenHash) {
        return dsl.update(table(TABLE))
                .set(field("is_revoked"), true)
                .set(field("revoked_at"), LocalDateTime.now())
                .where(field("token_hash").eq(tokenHash))
                .execute();
    }
    
    public int revokeAllByUserId(String userId) {
        return dsl.update(table(TABLE))
                .set(field("is_revoked"), true)
                .set(field("revoked_at"), LocalDateTime.now())
                .where(field("user_id").eq(userId))
                .where(field("is_revoked").eq(false))
                .execute();
    }
    
    public int deleteExpiredTokens() {
        return dsl.deleteFrom(table(TABLE))
                .where(field("expires_at").lt(LocalDateTime.now()))
                .execute();
    }
}

