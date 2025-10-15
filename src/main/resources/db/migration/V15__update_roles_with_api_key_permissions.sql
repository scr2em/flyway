-- Update existing roles with API key permissions
-- New permissions added (bits 31-33):
-- - API_KEY_VIEW (bit 31)      = 2147483648
-- - API_KEY_CREATE (bit 32)    = 4294967296
-- - API_KEY_DELETE (bit 33)    = 8589934592

-- Update Owner role - Full access to all features including API key management
-- Previous: 2147483641, New: 17179819177
UPDATE roles 
SET permissions = 17179819177,
    description = 'Full access to all organization features including billing, team management, mobile apps, builds, and API keys'
WHERE name = 'Owner';

-- Update Admin role - All permissions except billing.manage, includes all API key permissions
-- Previous: 2139095033, New: 17171430569
UPDATE roles 
SET permissions = 17171430569,
    description = 'Manages team, roles, deployments, mobile apps, builds, API keys, and can view billing. Cannot modify payment methods.'
WHERE name = 'Admin';

-- Update Developer role - Can create and view API keys for their apps
-- Previous: 2134450312, New: 8576901256
UPDATE roles 
SET permissions = 8576901256,
    description = 'Can deploy app updates, manage mobile applications, create API keys, upload and manage builds, view deployment history, and rollback changes. Read-only access to team.'
WHERE name = 'Developer';

-- Update Analyst role - Read-only access to API keys
-- Previous: 290664584, New: 2438148232
UPDATE roles 
SET permissions = 2438148232,
    description = 'Product managers and data analysts. Can view all data including mobile apps, builds, API keys, and manage user segments, but cannot deploy or upload builds.'
WHERE name = 'Analyst';

-- Update Support role - Read-only access to API keys
-- Previous: 286744584, New: 2434228232
UPDATE roles 
SET permissions = 2434228232,
    description = 'Customer support team. Can manage users, create invitations, view mobile apps, builds, API keys, and deployment status.'
WHERE name = 'Support';

