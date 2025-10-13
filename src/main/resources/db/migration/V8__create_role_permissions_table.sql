CREATE TABLE role_permissions (
    id CHAR(36) PRIMARY KEY,
    role_id CHAR(36) NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_code) REFERENCES permissions(code) ON DELETE CASCADE,
    UNIQUE KEY uk_role_permissions (role_id, permission_code),
    INDEX idx_role_permissions_role (role_id),
    INDEX idx_role_permissions_permission (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

