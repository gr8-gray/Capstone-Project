-- ===================================================================
-- SMART EXPENSE TRACKER - DATABASE USER SETUP
-- UMGC CMSC 495 Capstone Project - Group 3
-- Database Engineer: Michael Basye
-- ===================================================================
-- 
-- USAGE: Run this script as MySQL root user
-- mysql -u root -p < setup.sql
-- ===================================================================

-- Create database user for the application
CREATE USER IF NOT EXISTS 'expense_user'@'localhost' IDENTIFIED BY 'expense_password';

-- Grant necessary privileges
GRANT SELECT, INSERT, UPDATE, DELETE ON expense_db.* TO 'expense_user'@'localhost';
GRANT CREATE, ALTER, DROP ON expense_db.* TO 'expense_user'@'localhost';
GRANT EXECUTE ON expense_db.* TO 'expense_user'@'localhost';

-- For production, use a more secure password and restricted permissions
-- CREATE USER IF NOT EXISTS 'expense_user'@'localhost' IDENTIFIED BY 'SECURE_PASSWORD_HERE';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON expense_db.* TO 'expense_user'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify user creation
SELECT user, host FROM mysql.user WHERE user = 'expense_user';

-- Show granted privileges
SHOW GRANTS FOR 'expense_user'@'localhost';
