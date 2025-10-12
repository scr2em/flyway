package com.Flyway.Flyway.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
@RequiredArgsConstructor
public class RolePermissionRepository {
    
    private final DSLContext dsl;
    
    private static final String TABLE = "role_permissions";
    
    public List<Record> findByRoleId(String roleId) {
        return dsl.select()
                .from(table(TABLE))
                .join(table("permissions"))
                .on(field(TABLE + ".permission_id").eq(field("permissions.id")))
                .where(field(TABLE + ".role_id").eq(roleId))
                .fetch();
    }
    
    public String create(String roleId, String permissionId) {
        String id = UUID.randomUUID().toString();
        
        dsl.insertInto(table(TABLE))
                .columns(
                        field("id"),
                        field("role_id"),
                        field("permission_id"),
                        field("created_at")
                )
                .values(id, roleId, permissionId, LocalDateTime.now())
                .execute();
        
        return id;
    }
    
    public int deleteByRoleId(String roleId) {
        return dsl.deleteFrom(table(TABLE))
                .where(field("role_id").eq(roleId))
                .execute();
    }
    
    public int deleteByRoleIdAndPermissionId(String roleId, String permissionId) {
        return dsl.deleteFrom(table(TABLE))
                .where(field("role_id").eq(roleId)
                        .and(field("permission_id").eq(permissionId)))
                .execute();
    }
    
    public boolean existsByRoleIdAndPermissionId(String roleId, String permissionId) {
        return dsl.fetchExists(
                dsl.selectFrom(table(TABLE))
                        .where(field("role_id").eq(roleId)
                                .and(field("permission_id").eq(permissionId)))
        );
    }
}

