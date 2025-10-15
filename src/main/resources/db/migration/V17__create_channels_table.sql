-- Create channels table
CREATE TABLE channels (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    organization_id CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key to organizations
    CONSTRAINT fk_channels_organization 
        FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    
    -- Ensure channel names are unique within an organization
    CONSTRAINT uk_channels_name_org UNIQUE (name, organization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for organization lookups
CREATE INDEX idx_channels_organization_id ON channels(organization_id);

-- Create index for name lookups
CREATE INDEX idx_channels_name ON channels(name);

