-- Add id column to app_builds table
ALTER TABLE app_builds
ADD COLUMN id CHAR(36) NOT NULL FIRST;

-- Add unique index on id
ALTER TABLE app_builds
ADD UNIQUE INDEX idx_app_builds_id (id);

