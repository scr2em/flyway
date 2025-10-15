-- Update existing roles with mobile application permissions
-- New permissions added (bits 24-27):
-- - MOBILE_APP_READ (bit 24)
-- - MOBILE_APP_CREATE (bit 25)
-- - MOBILE_APP_UPDATE (bit 26)
-- - MOBILE_APP_DELETE (bit 27)

-- Update Owner role - Full access to all features including new mobile app permissions
UPDATE roles 
SET permissions = 268435455,
    description = 'Full access to all organization features including billing, team management, and mobile apps'
WHERE name = 'Owner';

-- Update Admin role - All permissions except billing.manage
UPDATE roles 
SET permissions = 260046847,
    description = 'Manages team, roles, deployments, mobile apps, and can view billing. Cannot modify payment methods.'
WHERE name = 'Admin';

-- Update Developer role - Deployment and mobile app management focused
UPDATE roles 
SET permissions = 255402120,
    description = 'Can deploy app updates, manage mobile applications, view deployment history, and rollback changes. Read-only access to team.'
WHERE name = 'Developer';

-- Update Analyst role - Analytics with mobile app viewing
UPDATE roles 
SET permissions = 22229128,
    description = 'Product managers and data analysts. Can view all data including mobile apps and manage user segments, but cannot deploy.'
WHERE name = 'Analyst';

-- Update Support role - User management with mobile app viewing
UPDATE roles 
SET permissions = 18309128,
    description = 'Customer support team. Can manage users, create invitations, view mobile apps, and view deployment status.'
WHERE name = 'Support';

