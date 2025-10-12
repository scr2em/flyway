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
    (UUID(), 'manage_users', 'Manage Users', 'Can add, edit, and remove users from the organization', 'auth'),
    (UUID(), 'manage_roles', 'Manage Roles', 'Can create and manage roles and permissions', 'auth'),
    (UUID(), 'view_members', 'View Members', 'Can view organization members', 'auth'),
    (UUID(), 'deploy_updates', 'Deploy Updates', 'Can deploy application updates', 'deployment'),
    (UUID(), 'view_deployments', 'View Deployments', 'Can view deployment history', 'deployment'),
    (UUID(), 'manage_billing', 'Manage Billing', 'Can manage billing and subscriptions', 'billing'),
    (UUID(), 'view_billing', 'View Billing', 'Can view billing information', 'billing');

