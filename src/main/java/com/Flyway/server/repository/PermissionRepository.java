package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.PermissionsRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.Flyway.server.jooq.tables.Permissions.PERMISSIONS;

@Repository
@RequiredArgsConstructor
public class PermissionRepository {
    
    private final DSLContext dsl;
    
    public Optional<PermissionsRecord> findByCode(String code) {
        return dsl.selectFrom(PERMISSIONS)
                .where(PERMISSIONS.CODE.eq(code))
                .fetchOptional();
    }
    
    public List<PermissionsRecord> findAll() {
        return dsl.selectFrom(PERMISSIONS)
                .fetch();
    }
    
    public List<PermissionsRecord> findByCategory(String category) {
        return dsl.selectFrom(PERMISSIONS)
                .where(PERMISSIONS.CATEGORY.eq(category))
                .fetch();
    }
    
    public List<PermissionsRecord> findByCodes(List<String> codes) {
        return dsl.selectFrom(PERMISSIONS)
                .where(PERMISSIONS.CODE.in(codes))
                .fetch();
    }
}

