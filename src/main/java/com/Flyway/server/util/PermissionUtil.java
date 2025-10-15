package com.Flyway.server.util;

import com.Flyway.server.model.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for bitwise permission operations
 */
public class PermissionUtil {
    
    /**
     * Check if a permission value contains a specific permission
     * @param permissionValue The combined permission value
     * @param permission The permission to check
     * @return true if the permission is present
     */
    public static boolean hasPermission(long permissionValue, Permission permission) {
        return (permissionValue & permission.getBitValue()) == permission.getBitValue();
    }
    
    /**
     * Check if a permission value contains a specific permission by code
     * @param permissionValue The combined permission value
     * @param permissionCode The permission code to check
     * @return true if the permission is present
     */
    public static boolean hasPermission(long permissionValue, String permissionCode) {
        Permission permission = Permission.fromCode(permissionCode);
        if (permission == null) {
            return false;
        }
        return hasPermission(permissionValue, permission);
    }
    
    /**
     * Add a permission to the permission value
     * @param permissionValue The current permission value
     * @param permission The permission to add
     * @return The new permission value
     */
    public static long addPermission(long permissionValue, Permission permission) {
        return permissionValue | permission.getBitValue();
    }
    
    /**
     * Add multiple permissions to the permission value
     * @param permissionValue The current permission value
     * @param permissions The permissions to add
     * @return The new permission value
     */
    public static long addPermissions(long permissionValue, Permission... permissions) {
        for (Permission permission : permissions) {
            permissionValue = addPermission(permissionValue, permission);
        }
        return permissionValue;
    }
    
    /**
     * Remove a permission from the permission value
     * @param permissionValue The current permission value
     * @param permission The permission to remove
     * @return The new permission value
     */
    public static long removePermission(long permissionValue, Permission permission) {
        return permissionValue & ~permission.getBitValue();
    }
    
    /**
     * Remove multiple permissions from the permission value
     * @param permissionValue The current permission value
     * @param permissions The permissions to remove
     * @return The new permission value
     */
    public static long removePermissions(long permissionValue, Permission... permissions) {
        for (Permission permission : permissions) {
            permissionValue = removePermission(permissionValue, permission);
        }
        return permissionValue;
    }
    
    /**
     * Convert a list of permission codes to a combined permission value
     * @param permissionCodes List of permission codes
     * @return The combined permission value
     */
    public static long fromCodes(List<String> permissionCodes) {
        long permissionValue = 0L;
        
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return permissionValue;
        }
        
        for (String code : permissionCodes) {
            Permission permission = Permission.fromCode(code);
            if (permission != null) {
                permissionValue = addPermission(permissionValue, permission);
            }
        }
        
        return permissionValue;
    }
    
    /**
     * Convert a permission value to a list of permission codes
     * @param permissionValue The combined permission value
     * @return List of permission codes
     */
    public static List<String> toCodes(long permissionValue) {
        List<String> codes = new ArrayList<>();
        
        for (Permission permission : Permission.values()) {
            if (hasPermission(permissionValue, permission)) {
                codes.add(permission.getCode());
            }
        }
        
        return codes;
    }
    
    /**
     * Convert a permission value to a list of Permission enums
     * @param permissionValue The combined permission value
     * @return List of Permission enums
     */
    public static List<Permission> toPermissions(long permissionValue) {
        List<Permission> permissions = new ArrayList<>();
        
        for (Permission permission : Permission.values()) {
            if (hasPermission(permissionValue, permission)) {
                permissions.add(permission);
            }
        }
        
        return permissions;
    }
    
    /**
     * Parse a permission string (which may be a number as a string) to a long value
     * @param permissionString The permission string from the frontend
     * @return The permission value as a long
     */
    public static long parsePermissionString(String permissionString) {
        if (permissionString == null || permissionString.trim().isEmpty()) {
            return 0L;
        }
        
        try {
            return Long.parseLong(permissionString.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    /**
     * Convert a permission value to a string
     * @param permissionValue The permission value
     * @return The permission value as a string
     */
    public static String toPermissionString(long permissionValue) {
        return String.valueOf(permissionValue);
    }
    
    /**
     * Check if the user has all of the specified permissions
     * @param permissionValue The user's permission value
     * @param requiredPermissions The permissions required
     * @return true if the user has all permissions
     */
    public static boolean hasAllPermissions(long permissionValue, Permission... requiredPermissions) {
        for (Permission permission : requiredPermissions) {
            if (!hasPermission(permissionValue, permission)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if the user has any of the specified permissions
     * @param permissionValue The user's permission value
     * @param requiredPermissions The permissions required (any of them)
     * @return true if the user has at least one permission
     */
    public static boolean hasAnyPermission(long permissionValue, Permission... requiredPermissions) {
        for (Permission permission : requiredPermissions) {
            if (hasPermission(permissionValue, permission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get all permissions as a combined bit value
     * @return A long value with all permissions set
     */
    public static long getAllPermissions() {
        long allPermissions = 0L;
        for (Permission permission : Permission.values()) {
            allPermissions |= permission.getBitValue();
        }
        return allPermissions;
    }
    
    // ========== Predefined Role Permissions ==========
    
    /**
     * Get Owner role permissions - Full access to all features
     * This role is automatically created and immutable
     * @return Permission value for Owner role
     */
    public static long getOwnerPermissions() {
        return getAllPermissions();
    }
    
    /**
     * Get Admin role permissions - All permissions except billing management
     * Can manage team, roles, deployments, mobile apps, and view billing
     * @return Permission value for Admin role
     */
    public static long getAdminPermissions() {
        return addPermissions(0L,
            Permission.ORGANIZATION_UPDATE,
            Permission.MEMBER_VIEW,
            Permission.MEMBER_ADD,
            Permission.MEMBER_REMOVE,
            Permission.MEMBER_UPDATE_ROLE,
            Permission.ROLE_VIEW,
            Permission.ROLE_CREATE,
            Permission.ROLE_UPDATE,
            Permission.ROLE_DELETE,
            Permission.ROLE_ASSIGN_PERMISSIONS,
            Permission.PERMISSION_VIEW,
            Permission.INVITATION_VIEW,
            Permission.INVITATION_CREATE,
            Permission.INVITATION_CANCEL,
            Permission.USER_VIEW,
            Permission.USER_UPDATE,
            Permission.USER_DELETE,
            Permission.DEPLOYMENT_CREATE,
            Permission.DEPLOYMENT_VIEW,
            Permission.DEPLOYMENT_ROLLBACK,
            Permission.BILLING_VIEW,
            Permission.MOBILE_APP_READ,
            Permission.MOBILE_APP_CREATE,
            Permission.MOBILE_APP_UPDATE,
            Permission.MOBILE_APP_DELETE
        );
    }
    
    /**
     * Get Developer role permissions - Can deploy and manage app updates
     * Focused on deployment operations and mobile app management with read access to organization
     * @return Permission value for Developer role
     */
    public static long getDeveloperPermissions() {
        return addPermissions(0L,
            Permission.MEMBER_VIEW,
            Permission.ROLE_VIEW,
            Permission.DEPLOYMENT_CREATE,
            Permission.DEPLOYMENT_VIEW,
            Permission.DEPLOYMENT_ROLLBACK,
            Permission.USER_VIEW,
            Permission.INVITATION_VIEW,
            Permission.MOBILE_APP_READ,
            Permission.MOBILE_APP_CREATE,
            Permission.MOBILE_APP_UPDATE,
            Permission.MOBILE_APP_DELETE
        );
    }
    
    /**
     * Get Analyst role permissions - Can view analytics and manage user segments
     * Product managers and data analysts who monitor but don't deploy
     * @return Permission value for Analyst role
     */
    public static long getAnalystPermissions() {
        return addPermissions(0L,
            Permission.MEMBER_VIEW,
            Permission.ROLE_VIEW,
            Permission.PERMISSION_VIEW,
            Permission.INVITATION_VIEW,
            Permission.USER_VIEW,
            Permission.USER_UPDATE,
            Permission.DEPLOYMENT_VIEW,
            Permission.BILLING_VIEW,
            Permission.MOBILE_APP_READ
        );
    }
    
    /**
     * Get Support role permissions - Can help users and manage access
     * Customer support team who assist users but don't deploy or manage billing
     * @return Permission value for Support role
     */
    public static long getSupportPermissions() {
        return addPermissions(0L,
            Permission.MEMBER_VIEW,
            Permission.INVITATION_VIEW,
            Permission.INVITATION_CREATE,
            Permission.USER_VIEW,
            Permission.USER_UPDATE,
            Permission.USER_DELETE,
            Permission.DEPLOYMENT_VIEW,
            Permission.MOBILE_APP_READ
        );
    }
}

