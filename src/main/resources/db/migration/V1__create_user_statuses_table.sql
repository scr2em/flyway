CREATE TABLE user_statuses (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_statuses_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default statuses
INSERT INTO user_statuses (id, code, label) VALUES
    (UUID(), 'active', 'Active'),
    (UUID(), 'suspended', 'Suspended'),
    (UUID(), 'deleted', 'Deleted');

