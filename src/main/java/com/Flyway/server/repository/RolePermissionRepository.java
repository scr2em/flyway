package com.Flyway.server.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.Flyway.server.jooq.tables.RolePermissions.ROLE_PERMISSIONS;
import static com.Flyway.server.jooq.tables.Permissions.PERMISSIONS;

@Repository
@RequiredArgsConstructor
public class RolePermissionRepository {
    
    private final DSLContext dsl;
    
    public List<Record> findByRoleId(String roleId) {
        return dsl.select()
                .from(ROLE_PERMISSIONS)
                .join(PERMISSIONS)
                .on(ROLE_PERMISSIONS.PERMISSION_CODE.eq(PERMISSIONS.CODE))
                .where(ROLE_PERMISSIONS.ROLE_ID.eq(roleId))
                .fetch();
    }
    
    public String create(String roleId, String permissionCode) {
        String id = UUID.randomUUID().toString();
        
        dsl.insertInto(ROLE_PERMISSIONS)
                .columns(
                        ROLE_PERMISSIONS.ID,
                        ROLE_PERMISSIONS.ROLE_ID,
                        ROLE_PERMISSIONS.PERMISSION_CODE,
                        ROLE_PERMISSIONS.CREATED_AT
                )
                .values(id, roleId, permissionCode, LocalDateTime.now())
                .execute();
        
        return id;
    }
    
    public int deleteByRoleId(String roleId) {
        return dsl.deleteFrom(ROLE_PERMISSIONS)
                .where(ROLE_PERMISSIONS.ROLE_ID.eq(roleId))
                .execute();
    }
    
    public int deleteByRoleIdAndPermissionCode(String roleId, String permissionCode) {
        return dsl.deleteFrom(ROLE_PERMISSIONS)
                .where(ROLE_PERMISSIONS.ROLE_ID.eq(roleId)
                        .and(ROLE_PERMISSIONS.PERMISSION_CODE.eq(permissionCode)))
                .execute();
    }
    
    public boolean existsByRoleIdAndPermissionCode(String roleId, String permissionCode) {
        return dsl.fetchExists(
                dsl.selectFrom(ROLE_PERMISSIONS)
                        .where(ROLE_PERMISSIONS.ROLE_ID.eq(roleId)
                                .and(ROLE_PERMISSIONS.PERMISSION_CODE.eq(permissionCode)))
        );
    }
}

