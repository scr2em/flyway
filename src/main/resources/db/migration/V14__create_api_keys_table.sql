CREATE TABLE api_keys (
    id CHAR(36) NOT NULL PRIMARY KEY,
    key_hash VARCHAR(255) NOT NULL UNIQUE,
    key_prefix VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    bundle_id VARCHAR(255) NOT NULL,
    organization_id CHAR(36) NOT NULL,
    created_by CHAR(36) NOT NULL,
    last_used_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_api_keys_bundle FOREIGN KEY (bundle_id) REFERENCES mobile_applications(bundle_id) ON DELETE CASCADE,
    CONSTRAINT fk_api_keys_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_api_keys_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_api_keys_bundle_id (bundle_id),
    INDEX idx_api_keys_organization (organization_id),
    INDEX idx_api_keys_creator (created_by),
    INDEX idx_api_keys_key_hash (key_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

