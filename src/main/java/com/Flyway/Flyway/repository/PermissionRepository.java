package com.Flyway.Flyway.repository;

import com.Flyway.Flyway.jooq.tables.records.PermissionsRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.Flyway.Flyway.jooq.tables.Permissions.PERMISSIONS;

@Repository
@RequiredArgsConstructor
public class PermissionRepository {
    
    private final DSLContext dsl;
    
    public Optional<PermissionsRecord> findById(String id) {
        return dsl.selectFrom(PERMISSIONS)
                .where(PERMISSIONS.ID.eq(id))
                .fetchOptional();
    }
    
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
    
    public List<PermissionsRecord> findByIds(List<String> ids) {
        return dsl.selectFrom(PERMISSIONS)
                .where(PERMISSIONS.ID.in(ids))
                .fetch();
    }
}

