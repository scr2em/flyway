-- Add audit.read permission to existing roles
-- audit.read = 1 << 38 = 274877906944

-- Owner: gets all permissions including audit.read
UPDATE roles 
SET permissions = permissions | 274877906944
WHERE name = 'Owner';

-- Admin: gets audit.read permission 
UPDATE roles 
SET permissions = permissions | 274877906944
WHERE name = 'Admin';

-- Developer: gets audit.read permission
UPDATE roles 
SET permissions = permissions | 274877906944
WHERE name = 'Developer';

-- Guest: does NOT get audit.read permission (view-only access)

