-- Drop the index first
DROP INDEX idx_users_invitation_status ON users;

-- Remove the invitation_status column from users table
ALTER TABLE users
DROP COLUMN invitation_status;

