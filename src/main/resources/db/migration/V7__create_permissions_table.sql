CREATE TABLE permissions (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    label VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_permissions_code (code),
    INDEX idx_permissions_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default permissions
INSERT INTO permissions (id, code, label, description, category) VALUES
    -- Organization permissions
    (UUID(), 'organization.view', 'View Organization', 'Can view organization details', 'organization'),
    (UUID(), 'organization.update', 'Update Organization', 'Can update organization information', 'organization'),
    (UUID(), 'organization.delete', 'Delete Organization', 'Can delete the organization', 'organization'),
    
    -- Member management permissions
    (UUID(), 'member.view', 'View Members', 'Can view organization members', 'member'),
    (UUID(), 'member.add', 'Add Members', 'Can add new members to the organization', 'member'),
    (UUID(), 'member.remove', 'Remove Members', 'Can remove members from the organization', 'member'),
    (UUID(), 'member.update_role', 'Update Member Role', 'Can update member roles', 'member'),
    
    -- Role management permissions
    (UUID(), 'role.view', 'View Roles', 'Can view roles', 'role'),
    (UUID(), 'role.create', 'Create Roles', 'Can create new roles', 'role'),
    (UUID(), 'role.update', 'Update Roles', 'Can update existing roles', 'role'),
    (UUID(), 'role.delete', 'Delete Roles', 'Can delete roles', 'role'),
    (UUID(), 'role.assign_permissions', 'Assign Permissions', 'Can assign permissions to roles', 'role'),
    
    -- Permission management
    (UUID(), 'permission.view', 'View Permissions', 'Can view all permissions', 'permission'),
    
    -- Invitation permissions
    (UUID(), 'invitation.view', 'View Invitations', 'Can view organization invitations', 'invitation'),
    (UUID(), 'invitation.create', 'Create Invitations', 'Can create invitations to join organization', 'invitation'),
    (UUID(), 'invitation.cancel', 'Cancel Invitations', 'Can cancel pending invitations', 'invitation'),
    
    -- User management permissions
    (UUID(), 'user.view', 'View Users', 'Can view user information', 'user'),
    (UUID(), 'user.update', 'Update Users', 'Can update user information', 'user'),
    (UUID(), 'user.delete', 'Delete Users', 'Can delete users', 'user'),
    
    -- Deployment permissions
    (UUID(), 'deployment.create', 'Deploy Updates', 'Can deploy application updates', 'deployment'),
    (UUID(), 'deployment.view', 'View Deployments', 'Can view deployment history', 'deployment'),
    (UUID(), 'deployment.rollback', 'Rollback Deployments', 'Can rollback deployments', 'deployment'),
    
    -- Billing permissions
    (UUID(), 'billing.view', 'View Billing', 'Can view billing information', 'billing'),
    (UUID(), 'billing.manage', 'Manage Billing', 'Can manage billing and subscriptions', 'billing');

