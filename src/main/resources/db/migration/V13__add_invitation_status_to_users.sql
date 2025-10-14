ALTER TABLE users
ADD COLUMN invitation_status VARCHAR(20) NOT NULL DEFAULT 'accepted'
COMMENT 'Status of the user invitation: pending, accepted, declined';

-- Create index for better query performance
CREATE INDEX idx_users_invitation_status ON users(invitation_status);

