-- ===================================================================
-- SMART EXPENSE TRACKER - DATABASE SCHEMA
-- UMGC CMSC 495 Capstone Project - Group 3
-- Database Engineer: Michael Basye
-- ===================================================================

-- Create database
CREATE DATABASE IF NOT EXISTS expense_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE expense_db;

-- ===================================================================
-- EXPENSES TABLE
-- Core table for tracking user expenses
-- ===================================================================
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for common queries (optimized for repository methods)
    INDEX idx_category (category),
    INDEX idx_date (date),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at),
    INDEX idx_category_date (category, date),
    INDEX idx_amount (amount),
    INDEX idx_description (description(100)),
    
    -- Constraints
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_description_not_empty CHECK (CHAR_LENGTH(description) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- CATEGORIES TABLE (Reference data)
-- Predefined expense categories
-- ===================================================================
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert predefined categories
INSERT INTO categories (name, description) VALUES
    ('Food & Dining', 'Restaurants, groceries, and food delivery'),
    ('Transportation', 'Gas, public transit, parking, and vehicle maintenance'),
    ('Shopping', 'Clothing, electronics, and general retail'),
    ('Entertainment', 'Movies, concerts, games, and recreational activities'),
    ('Bills & Utilities', 'Electricity, water, internet, and phone bills'),
    ('Healthcare', 'Medical expenses, pharmacy, and health insurance'),
    ('Education', 'Tuition, books, courses, and educational materials'),
    ('Travel', 'Hotels, flights, and vacation expenses'),
    ('Home & Garden', 'Home maintenance, repairs, and garden supplies'),
    ('Personal Care', 'Haircuts, cosmetics, and personal grooming'),
    ('Insurance', 'Life, auto, home, and other insurance premiums'),
    ('Investments', 'Stocks, bonds, retirement accounts, and investment fees'),
    ('Gifts & Donations', 'Charitable giving and presents'),
    ('Business', 'Work-related expenses and supplies'),
    ('Taxes', 'Income tax, property tax, and other tax payments'),
    ('Other', 'Miscellaneous expenses')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ===================================================================
-- USERS TABLE (For future authentication implementation)
-- Currently not implemented in backend, prepared for Phase 2
-- ===================================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- Add user_id to expenses when User authentication is implemented
-- Uncomment these lines in Phase 2:
-- ===================================================================
-- ALTER TABLE expenses 
--     ADD COLUMN user_id BIGINT AFTER id,
--     ADD CONSTRAINT fk_expense_user 
--         FOREIGN KEY (user_id) REFERENCES users(id) 
--         ON DELETE CASCADE,
--     ADD INDEX idx_user_id (user_id);

-- ===================================================================
-- BUDGET TABLE (For future budget tracking feature)
-- ===================================================================
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_category_month_year (category, month, year),
    INDEX idx_month_year (month, year),
    CONSTRAINT chk_month_valid CHECK (month BETWEEN 1 AND 12),
    CONSTRAINT chk_year_valid CHECK (year >= 2020),
    CONSTRAINT chk_budget_positive CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- VIEWS FOR COMMON QUERIES
-- ===================================================================

-- Monthly expense summary
CREATE OR REPLACE VIEW v_monthly_expenses AS
SELECT 
    YEAR(date) AS year,
    MONTH(date) AS month,
    category,
    COUNT(*) AS expense_count,
    SUM(amount) AS total_amount,
    AVG(amount) AS avg_amount,
    MIN(amount) AS min_amount,
    MAX(amount) AS max_amount
FROM expenses
GROUP BY YEAR(date), MONTH(date), category;

-- Category totals
CREATE OR REPLACE VIEW v_category_totals AS
SELECT 
    category,
    COUNT(*) AS expense_count,
    SUM(amount) AS total_amount,
    AVG(amount) AS avg_amount
FROM expenses
GROUP BY category
ORDER BY total_amount DESC;

-- Recent expenses (last 30 days)
CREATE OR REPLACE VIEW v_recent_expenses AS
SELECT 
    id,
    description,
    amount,
    category,
    date,
    created_at
FROM expenses
WHERE date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
ORDER BY date DESC, created_at DESC;

-- ===================================================================
-- SAMPLE DATA (for testing purposes)
-- Comment out in production
-- ===================================================================
INSERT INTO expenses (description, amount, category, date) VALUES
    ('Grocery shopping at Whole Foods', 125.50, 'Food & Dining', '2025-11-01'),
    ('Gas station fill-up', 45.00, 'Transportation', '2025-11-01'),
    ('Netflix subscription', 15.99, 'Subscriptions', '2025-11-01'),
    ('Lunch at cafe', 18.75, 'Food & Dining', '2025-11-02'),
    ('Electric bill payment', 89.50, 'Bills & Utilities', '2025-11-02'),
    ('New running shoes', 89.99, 'Shopping', '2025-11-03'),
    ('Movie tickets', 28.00, 'Entertainment', '2025-11-03')
ON DUPLICATE KEY UPDATE description = description;

-- ===================================================================
-- STORED PROCEDURES
-- ===================================================================

-- Get monthly spending summary
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_get_monthly_summary(
    IN p_year INT,
    IN p_month INT
)
BEGIN
    SELECT 
        category,
        COUNT(*) AS expense_count,
        SUM(amount) AS total_spent,
        AVG(amount) AS avg_expense,
        MIN(amount) AS min_expense,
        MAX(amount) AS max_expense
    FROM expenses
    WHERE YEAR(date) = p_year AND MONTH(date) = p_month
    GROUP BY category
    ORDER BY total_spent DESC;
END //
DELIMITER ;

-- Get expenses by date range
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_get_expenses_by_date_range(
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT 
        id,
        description,
        amount,
        category,
        date,
        created_at,
        updated_at
    FROM expenses
    WHERE date BETWEEN p_start_date AND p_end_date
    ORDER BY date DESC, created_at DESC;
END //
DELIMITER ;

-- Calculate category budget status
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_budget_status(
    IN p_year INT,
    IN p_month INT
)
BEGIN
    SELECT 
        b.category,
        b.amount AS budget_amount,
        COALESCE(SUM(e.amount), 0) AS spent_amount,
        b.amount - COALESCE(SUM(e.amount), 0) AS remaining,
        ROUND((COALESCE(SUM(e.amount), 0) / b.amount) * 100, 2) AS percent_used
    FROM budgets b
    LEFT JOIN expenses e ON b.category = e.category 
        AND YEAR(e.date) = b.year 
        AND MONTH(e.date) = b.month
    WHERE b.year = p_year AND b.month = p_month
    GROUP BY b.id, b.category, b.amount
    ORDER BY percent_used DESC;
END //
DELIMITER ;

-- ===================================================================
-- TRIGGERS
-- ===================================================================

-- Ensure amount is always positive
DELIMITER //
CREATE TRIGGER IF NOT EXISTS trg_before_insert_expense
BEFORE INSERT ON expenses
FOR EACH ROW
BEGIN
    IF NEW.amount <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Expense amount must be positive';
    END IF;
    
    IF NEW.date > CURDATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Expense date cannot be in the future';
    END IF;
END //
DELIMITER ;

-- ===================================================================
-- GRANT PRIVILEGES
-- Note: These will be executed separately in setup.sql
-- ===================================================================
