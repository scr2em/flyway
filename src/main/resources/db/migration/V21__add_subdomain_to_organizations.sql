-- Add subdomain field to organizations table
ALTER TABLE organizations 
ADD COLUMN subdomain VARCHAR(255) NULL AFTER name;

-- Add unique constraint for subdomain
ALTER TABLE organizations 
ADD CONSTRAINT uk_organizations_subdomain UNIQUE (subdomain);

-- Add index for subdomain lookups
CREATE INDEX idx_organizations_subdomain ON organizations (subdomain);
