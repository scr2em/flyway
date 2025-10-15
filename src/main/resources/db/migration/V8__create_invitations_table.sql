CREATE TABLE invitations (
    id CHAR(36) PRIMARY KEY,
    organization_id CHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role_id CHAR(36) NOT NULL,
    invited_by CHAR(36) NOT NULL,
    invitation_status_id CHAR(36) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    responded_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_invitations_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitations_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_invitations_inviter FOREIGN KEY (invited_by) REFERENCES users(id),
    CONSTRAINT fk_invitations_status FOREIGN KEY (invitation_status_id) REFERENCES invitation_statuses(id),
    INDEX idx_invitations_organization (organization_id),
    INDEX idx_invitations_email (email),
    INDEX idx_invitations_token (token),
    INDEX idx_invitations_status (invitation_status_id),
    INDEX idx_invitations_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

