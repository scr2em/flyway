CREATE TABLE invitation_statuses (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_invitation_statuses_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default invitation statuses
INSERT INTO invitation_statuses (id, code, label) VALUES
    (UUID(), 'pending', 'Pending'),
    (UUID(), 'accepted', 'Accepted'),
    (UUID(), 'rejected', 'Rejected'),
    (UUID(), 'expired', 'Expired');

