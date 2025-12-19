-- Migration script to add approved column to users table
-- Run this in PostgreSQL to fix the missing column error

-- Add approved column with default value false
ALTER TABLE users ADD COLUMN IF NOT EXISTS approved BOOLEAN DEFAULT false;

-- Update existing users - set admin users as approved
UPDATE users SET approved = true WHERE role = 'ADMIN';

-- Update all other existing users as approved (for existing data)
-- Comment out this line if you want existing non-admin users to need approval
UPDATE users SET approved = true WHERE role != 'ADMIN';

-- Verify the changes
SELECT id, username, full_name, role, approved FROM users;
