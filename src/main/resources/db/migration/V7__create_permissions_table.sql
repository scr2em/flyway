CREATE TABLE permissions (
    code VARCHAR(100) PRIMARY KEY,
    label VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_permissions_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default permissions
INSERT INTO permissions (code, label, description, category) VALUES
    -- Organization permissions
    ('organization.view', 'View Organization', 'Can view organization details', 'organization'),
    ('organization.update', 'Update Organization', 'Can update organization information', 'organization'),
    ('organization.delete', 'Delete Organization', 'Can delete the organization', 'organization'),
    
    -- Member management permissions
    ('member.view', 'View Members', 'Can view organization members', 'member'),
    ('member.add', 'Add Members', 'Can add new members to the organization', 'member'),
    ('member.remove', 'Remove Members', 'Can remove members from the organization', 'member'),
    ('member.update_role', 'Update Member Role', 'Can update member roles', 'member'),
    
    -- Role management permissions
    ('role.view', 'View Roles', 'Can view roles', 'role'),
    ('role.create', 'Create Roles', 'Can create new roles', 'role'),
    ('role.update', 'Update Roles', 'Can update existing roles', 'role'),
    ('role.delete', 'Delete Roles', 'Can delete roles', 'role'),
    ('role.assign_permissions', 'Assign Permissions', 'Can assign permissions to roles', 'role'),
    
    -- Permission management
    ('permission.view', 'View Permissions', 'Can view all permissions', 'permission'),
    
    -- Invitation permissions
    ('invitation.view', 'View Invitations', 'Can view organization invitations', 'invitation'),
    ('invitation.create', 'Create Invitations', 'Can create invitations to join organization', 'invitation'),
    ('invitation.cancel', 'Cancel Invitations', 'Can cancel pending invitations', 'invitation'),
    
    -- User management permissions
    ('user.view', 'View Users', 'Can view user information', 'user'),
    ('user.update', 'Update Users', 'Can update user information', 'user'),
    ('user.delete', 'Delete Users', 'Can delete users', 'user'),
    
    -- Deployment permissions
    ('deployment.create', 'Deploy Updates', 'Can deploy application updates', 'deployment'),
    ('deployment.view', 'View Deployments', 'Can view deployment history', 'deployment'),
    ('deployment.rollback', 'Rollback Deployments', 'Can rollback deployments', 'deployment'),
    
    -- Billing permissions
    ('billing.view', 'View Billing', 'Can view billing information', 'billing'),
    ('billing.manage', 'Manage Billing', 'Can manage billing and subscriptions', 'billing');

