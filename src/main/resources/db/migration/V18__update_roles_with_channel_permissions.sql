-- Update existing roles with Channel permissions
-- New permissions added (bits 34-37):
-- - CHANNEL_VIEW (bit 34)     = 17179869184
-- - CHANNEL_CREATE (bit 35)   = 34359738368
-- - CHANNEL_UPDATE (bit 36)   = 68719476736
-- - CHANNEL_DELETE (bit 37)   = 137438953472

-- Update Owner role - Full access to all features including channel management
-- Previous: 17179819177, New: 274877726889
UPDATE roles 
SET permissions = 274877726889,
    description = 'Full access to all organization features including billing, team management, mobile apps, builds, API keys, and channels'
WHERE name = 'Owner';

-- Update Admin role - All permissions except billing.manage, includes all channel permissions
-- Previous: 17171430569, New: 274869338281
UPDATE roles 
SET permissions = 274869338281,
    description = 'Manages team, roles, deployments, mobile apps, builds, API keys, channels, and can view billing. Cannot modify payment methods.'
WHERE name = 'Admin';

-- Update Developer role - Can create, update, and view channels
-- Previous: 8576901256, New: 128849808520
UPDATE roles 
SET permissions = 128849808520,
    description = 'Can deploy app updates, manage mobile applications, create API keys, upload and manage builds, manage channels, view deployment history, and rollback changes. Read-only access to team.'
WHERE name = 'Developer';

-- Update Analyst role - Read-only access to channels
-- Previous: 2438148232, New: 19617917960
UPDATE roles 
SET permissions = 19617917960,
    description = 'Product managers and data analysts. Can view all data including mobile apps, builds, API keys, channels, and manage user segments, but cannot deploy or upload builds.'
WHERE name = 'Analyst';

-- Update Support role - Read-only access to channels
-- Previous: 2434228232, New: 19613997960
UPDATE roles 
SET permissions = 19613997960,
    description = 'Customer support team. Can manage users, create invitations, view mobile apps, builds, API keys, channels, and deployment status.'
WHERE name = 'Support';

