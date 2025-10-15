package com.Flyway.server.model;

/**
 * Permission enum with bitwise values for Discord-style permission system.
 * Each permission has a unique bit position (power of 2).
 */
public enum Permission {
    // Organization permissions (bits 0-2)
    ORGANIZATION_UPDATE("organization.update", "Update Organization", "Can update organization information", "organization", 1L << 0),
    
    // Member management permissions (bits 3-6)
    MEMBER_VIEW("member.view", "View Members", "Can view organization members", "member", 1L << 3),
    MEMBER_ADD("member.add", "Add Members", "Can add new members to the organization", "member", 1L << 4),
    MEMBER_REMOVE("member.remove", "Remove Members", "Can remove members from the organization", "member", 1L << 5),
    MEMBER_UPDATE_ROLE("member.update_role", "Update Member Role", "Can update member roles", "member", 1L << 6),
    
    // Role management permissions (bits 7-11)
    ROLE_VIEW("role.view", "View Roles", "Can view roles", "role", 1L << 7),
    ROLE_CREATE("role.create", "Create Roles", "Can create new roles", "role", 1L << 8),
    ROLE_UPDATE("role.update", "Update Roles", "Can update existing roles", "role", 1L << 9),
    ROLE_DELETE("role.delete", "Delete Roles", "Can delete roles", "role", 1L << 10),
    ROLE_ASSIGN_PERMISSIONS("role.assign_permissions", "Assign Permissions", "Can assign permissions to roles", "role", 1L << 11),
    
    // Permission management (bit 12)
    PERMISSION_VIEW("permission.view", "View Permissions", "Can view all permissions", "permission", 1L << 12),
    
    // Invitation permissions (bits 13-15)
    INVITATION_VIEW("invitation.view", "View Invitations", "Can view organization invitations", "invitation", 1L << 13),
    INVITATION_CREATE("invitation.create", "Create Invitations", "Can create invitations to join organization", "invitation", 1L << 14),
    INVITATION_CANCEL("invitation.cancel", "Cancel Invitations", "Can cancel pending invitations", "invitation", 1L << 15),
    
    // User management permissions (bits 16-18)
    USER_VIEW("user.view", "View Users", "Can view user information", "user", 1L << 16),
    USER_UPDATE("user.update", "Update Users", "Can update user information", "user", 1L << 17),
    USER_DELETE("user.delete", "Delete Users", "Can delete users", "user", 1L << 18),
    
    // Deployment permissions (bits 19-21)
    DEPLOYMENT_CREATE("deployment.create", "Deploy Updates", "Can deploy application updates", "deployment", 1L << 19),
    DEPLOYMENT_VIEW("deployment.view", "View Deployments", "Can view deployment history", "deployment", 1L << 20),
    DEPLOYMENT_ROLLBACK("deployment.rollback", "Rollback Deployments", "Can rollback deployments", "deployment", 1L << 21),
    
    // Billing permissions (bits 22-23)
    BILLING_VIEW("billing.view", "View Billing", "Can view billing information", "billing", 1L << 22),
    BILLING_MANAGE("billing.manage", "Manage Billing", "Can manage billing and subscriptions", "billing", 1L << 23),
    
    // Mobile Application permissions (bits 24-27)
    MOBILE_APP_READ("mobile_app.read", "View Mobile Apps", "Can view mobile applications", "mobile_app", 1L << 24),
    MOBILE_APP_CREATE("mobile_app.create", "Create Mobile Apps", "Can create new mobile applications", "mobile_app", 1L << 25),
    MOBILE_APP_UPDATE("mobile_app.update", "Update Mobile Apps", "Can update mobile applications", "mobile_app", 1L << 26),
    MOBILE_APP_DELETE("mobile_app.delete", "Delete Mobile Apps", "Can delete mobile applications", "mobile_app", 1L << 27),
    
    // Build permissions (bits 28-30)
    BUILD_VIEW("build.view", "View Builds", "Can view application builds", "build", 1L << 28),
    BUILD_UPLOAD("build.upload", "Upload Builds", "Can upload new builds", "build", 1L << 29),
    BUILD_DELETE("build.delete", "Delete Builds", "Can delete builds", "build", 1L << 30),
    
    // API Key permissions (bits 31-34)
    API_KEY_VIEW("api_key.view", "View API Keys", "Can view API keys", "api_key", 1L << 31),
    API_KEY_CREATE("api_key.create", "Create API Keys", "Can create new API keys", "api_key", 1L << 32),
    API_KEY_DELETE("api_key.delete", "Delete API Keys", "Can delete API keys", "api_key", 1L << 33),
    
    // Channel permissions (bits 34-37)
    CHANNEL_VIEW("channel.view", "View Channels", "Can view channels", "channel", 1L << 34),
    CHANNEL_CREATE("channel.create", "Create Channels", "Can create new channels", "channel", 1L << 35),
    CHANNEL_UPDATE("channel.update", "Update Channels", "Can update channels", "channel", 1L << 36),
    CHANNEL_DELETE("channel.delete", "Delete Channels", "Can delete channels", "channel", 1L << 37);
    
    private final String code;
    private final String label;
    private final String description;
    private final String category;
    private final long bitValue;
    
    Permission(String code, String label, String description, String category, long bitValue) {
        this.code = code;
        this.label = label;
        this.description = description;
        this.category = category;
        this.bitValue = bitValue;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public long getBitValue() {
        return bitValue;
    }
    
    /**
     * Get a permission by its code
     * @param code The permission code (e.g., "organization.update")
     * @return The Permission enum, or null if not found
     */
    public static Permission fromCode(String code) {
        for (Permission permission : values()) {
            if (permission.code.equals(code)) {
                return permission;
            }
        }
        return null;
    }
    
    /**
     * Get the resource part of the permission (e.g., "organization" from "organization.update")
     */
    public String getResource() {
        String[] parts = code.split("\\.", 2);
        return parts.length > 0 ? parts[0] : "";
    }
    
    /**
     * Get the action part of the permission (e.g., "update" from "organization.update")
     */
    public String getAction() {
        String[] parts = code.split("\\.", 2);
        return parts.length > 1 ? parts[1] : "";
    }
}

