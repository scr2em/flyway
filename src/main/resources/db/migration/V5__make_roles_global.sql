CREATE TABLE roles (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    permissions BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert the 5 predefined global roles
INSERT INTO roles (id, name, description, permissions) VALUES
    -- Owner: All permissions (16777215)
    (UUID(), 'Owner', 'Full access to all organization features including billing and team management', 16777215),
    
    -- Admin: All permissions except billing.manage (8388607)
    (UUID(), 'Admin', 'Manages team, roles, deployments, and can view billing. Cannot modify payment methods.', 8388607),
    
    -- Developer: Deployment focused (3743880)
    (UUID(), 'Developer', 'Can deploy app updates, view deployment history, and rollback changes. Read-only access to team.', 3743880),
    
    -- Analyst: Analytics and user management (5602912)
    (UUID(), 'Analyst', 'Product managers and data analysts. Can view all data and manage user segments, but cannot deploy.', 5602912),
    
    -- Support: User and invitation management (1532912)
    (UUID(), 'Support', 'Customer support team. Can manage users, create invitations, and view deployment status.', 1532912);


