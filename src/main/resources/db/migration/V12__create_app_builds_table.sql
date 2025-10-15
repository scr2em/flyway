CREATE TABLE app_builds (
    organization_id CHAR(36) NOT NULL,
    bundle_id VARCHAR(255) NOT NULL,
    commit_hash VARCHAR(255) NOT NULL,
    branch_name VARCHAR(255) NOT NULL,
    commit_message TEXT NULL,
    build_size BIGINT NOT NULL,
    build_url VARCHAR(1024) NOT NULL,
    native_version VARCHAR(50) NOT NULL,
    uploaded_by CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (organization_id, bundle_id, commit_hash),
    CONSTRAINT fk_app_builds_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_builds_mobile_app FOREIGN KEY (bundle_id) REFERENCES mobile_applications(bundle_id) ON DELETE CASCADE,
    CONSTRAINT fk_app_builds_uploader FOREIGN KEY (uploaded_by) REFERENCES users(id),
    INDEX idx_app_builds_created_at (created_at DESC),
    INDEX idx_app_builds_bundle_id (bundle_id),
    INDEX idx_app_builds_organization (organization_id),
    INDEX idx_app_builds_uploaded_by (uploaded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

