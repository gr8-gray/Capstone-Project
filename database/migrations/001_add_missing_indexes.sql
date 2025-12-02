-- ===================================================================
-- MIGRATION: Add Missing Database Indexes
-- Date: November 30, 2025
-- Author: AI Code Analysis
-- Description: Adds composite indexes for optimal query performance
-- ===================================================================

USE expense_db;

-- ===================================================================
-- STEP 1: Add user_id column to expenses (if not exists)
-- ===================================================================
ALTER TABLE expenses 
    ADD COLUMN IF NOT EXISTS user_id BIGINT AFTER id;

-- Add foreign key constraint
ALTER TABLE expenses
    ADD CONSTRAINT IF NOT EXISTS fk_expense_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE;

-- ===================================================================
-- STEP 2: Add missing indexes to expenses table
-- ===================================================================

-- Standalone user_id index (critical for JOIN operations)
ALTER TABLE expenses
    ADD INDEX IF NOT EXISTS idx_user_id (user_id);

-- Composite indexes for user-filtered queries
ALTER TABLE expenses
    ADD INDEX IF NOT EXISTS idx_user_category (user_id, category);

ALTER TABLE expenses
    ADD INDEX IF NOT EXISTS idx_user_date (user_id, date);

ALTER TABLE expenses
    ADD INDEX IF NOT EXISTS idx_user_category_date (user_id, category, date);

ALTER TABLE expenses
    ADD INDEX IF NOT EXISTS idx_user_amount (user_id, amount);

ALTER TABLE expenses
    ADD INDEX IF NOT EXISTS idx_user_date_amount (user_id, date, amount);

-- ===================================================================
-- STEP 3: Add user_id to budgets table (if not exists)
-- ===================================================================
ALTER TABLE budgets 
    ADD COLUMN IF NOT EXISTS user_id BIGINT AFTER id;

-- Add foreign key constraint
ALTER TABLE budgets
    ADD CONSTRAINT IF NOT EXISTS fk_budget_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE;

-- Add user_id index
ALTER TABLE budgets
    ADD INDEX IF NOT EXISTS idx_user_id (user_id);

-- Composite indexes for budget queries
ALTER TABLE budgets
    ADD INDEX IF NOT EXISTS idx_user_category (user_id, category);

ALTER TABLE budgets
    ADD INDEX IF NOT EXISTS idx_user_dates (user_id, start_date, end_date);

ALTER TABLE budgets
    ADD INDEX IF NOT EXISTS idx_category_dates (category, start_date, end_date);

-- ===================================================================
-- STEP 4: Add composite indexes to budget_alerts table
-- ===================================================================

-- For queries filtering by budget and read status
ALTER TABLE budget_alerts
    ADD INDEX IF NOT EXISTS idx_budget_is_read (budget_id, is_read);

-- For unread alerts ordered by creation time
ALTER TABLE budget_alerts
    ADD INDEX IF NOT EXISTS idx_is_read_created (is_read, created_at);

-- For alerts filtered by level and ordered by creation time
ALTER TABLE budget_alerts
    ADD INDEX IF NOT EXISTS idx_alert_level_created (alert_level, created_at);

-- For critical unread alerts query (covering index)
ALTER TABLE budget_alerts
    ADD INDEX IF NOT EXISTS idx_is_read_alert_level (is_read, alert_level, created_at);

-- For cleanup queries (deleteOldReadAlerts)
ALTER TABLE budget_alerts
    ADD INDEX IF NOT EXISTS idx_is_read_read_at (is_read, read_at);

-- ===================================================================
-- STEP 5: Verify indexes were created
-- ===================================================================
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'expense_db'
    AND TABLE_NAME IN ('expenses', 'budgets', 'budget_alerts')
    AND INDEX_NAME LIKE 'idx_%'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- ===================================================================
-- PERFORMANCE ANALYSIS
-- ===================================================================
-- After running this migration, you can analyze query performance with:
-- 
-- EXPLAIN SELECT * FROM expenses WHERE user_id = 1 AND category = 'Food & Dining';
-- EXPLAIN SELECT * FROM expenses WHERE user_id = 1 AND date BETWEEN '2025-01-01' AND '2025-12-31';
-- EXPLAIN SELECT * FROM budget_alerts WHERE is_read = false ORDER BY created_at DESC;
-- 
-- All queries should show "Using index" in the Extra column.
-- ===================================================================
