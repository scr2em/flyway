-- Create audit_logs table for tracking all actions in the system
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    organization_id VARCHAR(36),
    action VARCHAR(255) NOT NULL,
    resource_type VARCHAR(100),
    resource_id VARCHAR(36),
    resource_name VARCHAR(255),
    http_method VARCHAR(10),
    endpoint VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_body TEXT,
    response_status INT,
    error_message TEXT,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_organization_id (organization_id),
    INDEX idx_created_at (created_at),
    INDEX idx_resource_type (resource_type),
    INDEX idx_action (action),
    INDEX idx_resource_id (resource_id),
    INDEX idx_org_created (organization_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

