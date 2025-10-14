ALTER TABLE users
ADD COLUMN temp_password BOOLEAN NOT NULL DEFAULT FALSE
COMMENT 'Indicates if the user is using a temporary password that needs to be changed';

