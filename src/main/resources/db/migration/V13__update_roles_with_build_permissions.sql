-- Update existing roles with build permissions
-- New permissions added (bits 28-30):
-- - BUILD_VIEW (bit 28)      = 268435456
-- - BUILD_UPLOAD (bit 29)    = 536870912
-- - BUILD_DELETE (bit 30)    = 1073741824

-- Update Owner role - Full access to all features including new build permissions
-- Previous: 268435455, New: 2147483641
UPDATE roles 
SET permissions = 2147483641,
    description = 'Full access to all organization features including billing, team management, mobile apps, and builds'
WHERE name = 'Owner';

-- Update Admin role - All permissions except billing.manage, includes all build permissions
-- Previous: 260046847, New: 2139095033
UPDATE roles 
SET permissions = 2139095033,
    description = 'Manages team, roles, deployments, mobile apps, builds, and can view billing. Cannot modify payment methods.'
WHERE name = 'Admin';

-- Update Developer role - Deployment, mobile app, and build management focused
-- Previous: 255402120, New: 2134450312
UPDATE roles 
SET permissions = 2134450312,
    description = 'Can deploy app updates, manage mobile applications, upload and manage builds, view deployment history, and rollback changes. Read-only access to team.'
WHERE name = 'Developer';

-- Update Analyst role - Analytics with mobile app and build viewing
-- Previous: 22229128, New: 290664584
UPDATE roles 
SET permissions = 290664584,
    description = 'Product managers and data analysts. Can view all data including mobile apps, builds, and manage user segments, but cannot deploy or upload builds.'
WHERE name = 'Analyst';

-- Update Support role - User management with mobile app and build viewing
-- Previous: 18309128, New: 286744584
UPDATE roles 
SET permissions = 286744584,
    description = 'Customer support team. Can manage users, create invitations, view mobile apps, builds, and deployment status.'
WHERE name = 'Support';

