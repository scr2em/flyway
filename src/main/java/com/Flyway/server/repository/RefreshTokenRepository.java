package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.RefreshTokensRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.Flyway.server.jooq.tables.RefreshTokens.REFRESH_TOKENS;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    
    private final DSLContext dsl;
    
    public Optional<RefreshTokensRecord> findById(String id) {
        return dsl.selectFrom(REFRESH_TOKENS)
                .where(REFRESH_TOKENS.ID.eq(id))
                .fetchOptional();
    }
    
    public Optional<RefreshTokensRecord> findByTokenHash(String tokenHash) {
        return dsl.selectFrom(REFRESH_TOKENS)
                .where(REFRESH_TOKENS.TOKEN_HASH.eq(tokenHash)
                        .and(REFRESH_TOKENS.IS_REVOKED.eq((byte) 0)))
                .fetchOptional();
    }
    
    public List<RefreshTokensRecord> findByUserId(String userId) {
        return dsl.selectFrom(REFRESH_TOKENS)
                .where(REFRESH_TOKENS.USER_ID.eq(userId))
                .fetch();
    }
    
    public String create(String userId, String tokenHash, LocalDateTime expiresAt, String deviceInfo, String ipAddress) {
        String id = UUID.randomUUID().toString();
        
        RefreshTokensRecord record = dsl.newRecord(REFRESH_TOKENS);
        record.setId(id);
        record.setUserId(userId);
        record.setTokenHash(tokenHash);
        record.setIsRevoked((byte) 0); // false
        record.setDeviceInfo(deviceInfo);
        record.setIpAddress(ipAddress);
        record.setExpiresAt(expiresAt);
        record.setCreatedAt(LocalDateTime.now());
        record.store();
        
        return id;
    }
    
    public int revoke(String id) {
        return dsl.update(REFRESH_TOKENS)
                .set(REFRESH_TOKENS.IS_REVOKED, (byte) 1) // true
                .set(REFRESH_TOKENS.REVOKED_AT, LocalDateTime.now())
                .where(REFRESH_TOKENS.ID.eq(id))
                .execute();
    }
    
    public int revokeByTokenHash(String tokenHash) {
        return dsl.update(REFRESH_TOKENS)
                .set(REFRESH_TOKENS.IS_REVOKED, (byte) 1) // true
                .set(REFRESH_TOKENS.REVOKED_AT, LocalDateTime.now())
                .where(REFRESH_TOKENS.TOKEN_HASH.eq(tokenHash))
                .execute();
    }
    
    public int revokeAllByUserId(String userId) {
        return dsl.update(REFRESH_TOKENS)
                .set(REFRESH_TOKENS.IS_REVOKED, (byte) 1) // true
                .set(REFRESH_TOKENS.REVOKED_AT, LocalDateTime.now())
                .where(REFRESH_TOKENS.USER_ID.eq(userId)
                        .and(REFRESH_TOKENS.IS_REVOKED.eq((byte) 0)))
                .execute();
    }
    
    public int deleteExpiredTokens() {
        return dsl.deleteFrom(REFRESH_TOKENS)
                .where(REFRESH_TOKENS.EXPIRES_AT.lt(LocalDateTime.now()))
                .execute();
    }
}

