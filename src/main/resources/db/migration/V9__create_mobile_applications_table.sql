CREATE TABLE mobile_applications (
    id CHAR(36) PRIMARY KEY,
    bundle_id VARCHAR(255) NOT NULL UNIQUE,
    organization_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    created_by CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_mobile_apps_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_mobile_apps_creator FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_mobile_apps_organization (organization_id),
    INDEX idx_mobile_apps_bundle_id (bundle_id),
    INDEX idx_mobile_apps_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

