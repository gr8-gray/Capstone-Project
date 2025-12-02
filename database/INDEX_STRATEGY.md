# Database Index Strategy - Quick Reference

## Index Inventory

### Expenses Table (Production-Critical)
| Index Name | Columns | Query Pattern | Usage |
|------------|---------|---------------|-------|
| `idx_user_id` | user_id | Single user lookup | JOIN operations, user filtering |
| `idx_user_category` | user_id, category | User + category filter | Category reports per user |
| `idx_user_date` | user_id, date | User + date range | Monthly/yearly summaries |
| `idx_user_category_date` | user_id, category, date | User + category + date | Detailed category spending over time |
| `idx_user_amount` | user_id, amount | User + amount filter | High-value expense queries |
| `idx_user_date_amount` | user_id, date, amount | User + date + amount | Period spending analysis |
| `idx_category` | category | Legacy global category | Non-user specific queries |
| `idx_date` | date | Legacy global date | Non-user specific queries |
| `idx_category_date` | category, date | Global category trends | Admin/analytics |

### Budgets Table
| Index Name | Columns | Query Pattern | Usage |
|------------|---------|---------------|-------|
| `idx_user_id` | user_id | User budget lookup | Budget list per user |
| `idx_user_category` | user_id, category | User + category budget | Check budget for category |
| `idx_user_dates` | user_id, start_date, end_date | Active budgets for user | Find overlapping budgets |
| `idx_category_dates` | category, start_date, end_date | Date range overlap | Budget conflict detection |

### Budget Alerts Table
| Index Name | Columns | Query Pattern | Usage |
|------------|---------|---------------|-------|
| `idx_budget_id` | budget_id | Single budget alerts | Alert history per budget |
| `idx_budget_is_read` | budget_id, is_read | Budget + read status | Unread alerts for budget |
| `idx_is_read_created` | is_read, created_at | Unread sorted by date | Dashboard unread alerts |
| `idx_alert_level_created` | alert_level, created_at | Alerts by severity | Critical alerts list |
| `idx_is_read_alert_level` | is_read, alert_level, created_at | Covering index | Critical unread query (no table access) |
| `idx_is_read_read_at` | is_read, read_at | Read status + timestamp | Cleanup old read alerts |

## Query Optimization Examples

### ✅ Optimized Queries

#### 1. User Expense List (uses idx_user_date)
```java
// Repository method
@Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :start AND :end")
List<Expense> findByUserIdAndDateBetween(@Param("userId") Long userId, 
                                          @Param("start") LocalDate start, 
                                          @Param("end") LocalDate end);

// MySQL Execution Plan:
// key: idx_user_date
// Extra: Using index condition
```

#### 2. Category Summary (uses idx_user_category_date)
```java
// Repository method
@Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId " +
       "AND e.category = :category AND e.date BETWEEN :start AND :end")
BigDecimal sumAmountByUserIdAndCategoryAndDateRange(...);

// MySQL Execution Plan:
// key: idx_user_category_date
// Extra: Using index for group-by
```

#### 3. Unread Critical Alerts (uses idx_is_read_alert_level - COVERING INDEX)
```java
// Repository method
@Query("SELECT ba FROM BudgetAlert ba WHERE ba.isRead = false " +
       "AND ba.alertLevel IN ('DANGER', 'CRITICAL') ORDER BY ba.createdAt DESC")
List<BudgetAlert> findCriticalUnreadAlerts();

// MySQL Execution Plan:
// key: idx_is_read_alert_level
// Extra: Using index (NO TABLE ACCESS - all data in index!)
```

### ❌ Anti-Patterns (Index Misses)

#### 1. Wrong Column Order
```java
// ❌ BAD - filters by category first, idx_user_category NOT used
findByCategoryAndUserId(String category, Long userId);

// ✅ GOOD - filters by user_id first, uses idx_user_category
findByUserIdAndCategory(Long userId, String category);
```

#### 2. Function on Indexed Column
```java
// ❌ BAD - YEAR() function prevents index usage
@Query("SELECT e FROM Expense e WHERE YEAR(e.date) = :year AND e.user.id = :userId")

// ✅ GOOD - date range uses idx_user_date
@Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
       "AND e.date BETWEEN :startOfYear AND :endOfYear")
```

#### 3. OR Conditions on Different Columns
```java
// ❌ BAD - OR prevents index usage, falls back to full table scan
@Query("SELECT e FROM Expense e WHERE e.user.id = :userId OR e.category = :category")

// ✅ GOOD - separate queries or UNION if really needed
findByUserId() + findByCategory() (merge in service layer)
```

## Leftmost Prefix Rule

Composite indexes can be used for queries that match columns **from left to right**:

**Index: idx_user_category_date (user_id, category, date)**

| Query Columns | Index Used? | Explanation |
|---------------|-------------|-------------|
| user_id | ✅ YES | Matches leftmost column |
| user_id, category | ✅ YES | Matches first 2 columns |
| user_id, category, date | ✅ YES | Matches all columns (best) |
| category | ❌ NO | Skips user_id (leftmost) |
| date | ❌ NO | Skips user_id and category |
| category, date | ❌ NO | Skips user_id |
| user_id, date | ⚠️ PARTIAL | Uses user_id, but not date (gap in middle) |

## Measuring Index Effectiveness

### EXPLAIN Query Analysis
```sql
-- Check if index is used
EXPLAIN SELECT * FROM expenses 
WHERE user_id = 1 AND category = 'Food & Dining';

-- Look for:
-- key: idx_user_category (✅ good)
-- key: NULL (❌ no index used)
-- Extra: Using index (✅ covering index - best)
-- Extra: Using index condition (✅ good)
-- Extra: Using where (⚠️ filtering after retrieval)
-- Extra: Using filesort (❌ slow - no index on ORDER BY column)
```

### Slow Query Log
```properties
# Enable in application.properties
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG
```

### Index Cardinality Check
```sql
-- High cardinality = good selectivity
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY,
    COLUMN_NAME
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'expense_db'
ORDER BY TABLE_NAME, INDEX_NAME;
```

## When to Add New Indexes

### ✅ Add Index When:
1. Query appears in slow query log repeatedly
2. EXPLAIN shows full table scan (`type: ALL`)
3. New feature introduces frequent filtering on column combinations
4. Cardinality is high (many distinct values)

### ❌ DON'T Add Index When:
1. Table has < 1000 rows (index overhead not worth it)
2. Column has low cardinality (e.g., boolean with 50/50 split)
3. Query is rarely executed (< once per day)
4. Write performance is more critical than read performance

## Maintenance Commands

### Analyze Index Usage
```sql
-- Check index statistics
SHOW INDEX FROM expenses;

-- Find unused indexes (MySQL 8.0+)
SELECT * FROM sys.schema_unused_indexes
WHERE object_schema = 'expense_db';
```

### Rebuild Fragmented Indexes
```sql
-- After bulk operations
OPTIMIZE TABLE expenses;
ANALYZE TABLE expenses;
```

## Migration Checklist

When adding new query methods:

- [ ] Identify all filter columns in WHERE clause
- [ ] Check column order matches index leftmost prefix
- [ ] Verify index exists with `SHOW INDEX FROM table_name`
- [ ] If missing, add to `schema.sql` AND create migration file
- [ ] Run `EXPLAIN` on representative query
- [ ] Update entity `@Table(indexes = {...})` annotations
- [ ] Document in repository interface JavaDoc

## Performance Targets

| Query Type | Expected Performance |
|------------|---------------------|
| Primary key lookup | < 1ms |
| Indexed single-table query | < 5ms |
| Indexed multi-table JOIN (2-3 tables) | < 20ms |
| Aggregation with indexes | < 50ms |
| Full table scan (acceptable) | < 100ms for tables < 10k rows |

If queries exceed these targets, investigate indexing strategy.
