package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.UsersRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.Flyway.server.jooq.tables.Users.USERS;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    
    private final DSLContext dsl;
    
    public Optional<UsersRecord> findById(String id) {
        return dsl.selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .fetchOptional();
    }
    
    public Optional<UsersRecord> findByEmail(String email) {
        return dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOptional();
    }
    
    public List<UsersRecord> findAll() {
        return dsl.selectFrom(USERS)
                .fetch();
    }
    
    public String create(String firstName, String lastName, String email, String passwordHash, String userStatusId) {
        return create(firstName, lastName, email, passwordHash, userStatusId, false);
    }
    
    public String create(String firstName, String lastName, String email, String passwordHash, String userStatusId, boolean tempPassword) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        UsersRecord record = dsl.newRecord(USERS);
        record.setId(id);
        record.setFirstName(firstName);
        record.setLastName(lastName);
        record.setEmail(email);
        record.setPasswordHash(passwordHash);
        record.setUserStatusId(userStatusId);
        record.setEmailVerified((byte) 0); // false
        record.setTempPassword((byte) (tempPassword ? 1 : 0));
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.store();
        
        return id;
    }
    
    public int update(String id, String firstName, String lastName) {
        return dsl.update(USERS)
                .set(USERS.FIRST_NAME, firstName)
                .set(USERS.LAST_NAME, lastName)
                .set(USERS.UPDATED_AT, LocalDateTime.now())
                .where(USERS.ID.eq(id))
                .execute();
    }
    
    public int updateLastLogin(String id) {
        return dsl.update(USERS)
                .set(USERS.LAST_LOGIN_AT, LocalDateTime.now())
                .where(USERS.ID.eq(id))
                .execute();
    }
    
    public int verifyEmail(String id) {
        LocalDateTime now = LocalDateTime.now();
        return dsl.update(USERS)
                .set(USERS.EMAIL_VERIFIED, (byte) 1) // true
                .set(USERS.EMAIL_VERIFIED_AT, now)
                .set(USERS.UPDATED_AT, now)
                .where(USERS.ID.eq(id))
                .execute();
    }
    
    public int updatePassword(String id, String passwordHash) {
        return dsl.update(USERS)
                .set(USERS.PASSWORD_HASH, passwordHash)
                .set(USERS.TEMP_PASSWORD, (byte) 0) // false - no longer temp password
                .set(USERS.UPDATED_AT, LocalDateTime.now())
                .where(USERS.ID.eq(id))
                .execute();
    }
    
    public int updatePasswordWithTempFlag(String id, String passwordHash, boolean isTempPassword) {
        return dsl.update(USERS)
                .set(USERS.PASSWORD_HASH, passwordHash)
                .set(USERS.TEMP_PASSWORD, (byte) (isTempPassword ? 1 : 0))
                .set(USERS.UPDATED_AT, LocalDateTime.now())
                .where(USERS.ID.eq(id))
                .execute();
    }
    
    public int updateTempPasswordFlag(String id, boolean isTempPassword) {
        return dsl.update(USERS)
                .set(USERS.TEMP_PASSWORD, (byte) (isTempPassword ? 1 : 0))
                .set(USERS.UPDATED_AT, LocalDateTime.now())
                .where(USERS.ID.eq(id))
                .execute();
    }
    
    public int delete(String id) {
        return dsl.deleteFrom(USERS)
                .where(USERS.ID.eq(id))
                .execute();
    }
    
    public boolean existsByEmail(String email) {
        return dsl.fetchExists(
                dsl.selectFrom(USERS)
                        .where(USERS.EMAIL.eq(email))
        );
    }
}

