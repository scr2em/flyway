-- Remove organization.view and organization.delete permissions as they are no longer needed
-- All organization members can view the organization, and we don't support organization deletion

-- First, remove any role_permissions associations
DELETE FROM role_permissions 
WHERE permission_code IN ('organization.view', 'organization.delete');

-- Then remove the permissions themselves
DELETE FROM permissions 
WHERE code IN ('organization.view', 'organization.delete');

