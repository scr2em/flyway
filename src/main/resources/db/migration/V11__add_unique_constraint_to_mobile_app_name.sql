-- Add unique constraint on mobile application name per organization
ALTER TABLE mobile_applications
ADD CONSTRAINT uq_mobile_apps_org_name UNIQUE (organization_id, name);

