# Database Quick Reference Guide
## Smart Expense Tracker - Common Operations

**Database Engineer:** Michael Basye  
**UMGC CMSC 495 Capstone Project - Group 3**

---

## 🚀 Quick Start

### Setup (One-time)
```bash
cd database
mysql -u root -p < setup.sql
mysql -u root -p < schema.sql
```

### Connect to Database
```bash
mysql -u expense_user -p expense_db
# Password: expense_password
```

---

## 📊 Common Queries

### View All Expenses
```sql
SELECT * FROM expenses ORDER BY date DESC LIMIT 10;
```

### Add New Expense
```sql
INSERT INTO expenses (description, amount, category, date)
VALUES ('Coffee at Starbucks', 5.75, 'Food & Dining', CURDATE());
```

### Monthly Summary
```sql
CALL sp_get_monthly_summary(2025, 11);
-- OR use the view
SELECT * FROM v_monthly_expenses WHERE year = 2025 AND month = 11;
```

### Category Totals
```sql
SELECT * FROM v_category_totals;
```

### Recent Expenses (Last 30 Days)
```sql
SELECT * FROM v_recent_expenses;
```

### Expenses by Date Range
```sql
CALL sp_get_expenses_by_date_range('2025-11-01', '2025-11-30');
```

### Find Expensive Items
```sql
SELECT * FROM expenses 
WHERE amount > 100 
ORDER BY amount DESC;
```

### Search by Description
```sql
SELECT * FROM expenses 
WHERE description LIKE '%coffee%' 
ORDER BY date DESC;
```

---

## 🔧 Maintenance

### Backup Database
```bash
mysqldump -u expense_user -p expense_db > backup_$(date +%Y%m%d).sql
```

### Restore Database
```bash
mysql -u expense_user -p expense_db < backup_20251103.sql
```

### Check Database Size
```sql
SELECT 
    table_name,
    ROUND((data_length + index_length) / 1024 / 1024, 2) AS size_mb
FROM information_schema.tables
WHERE table_schema = 'expense_db';
```

### Reset Sample Data
```sql
DELETE FROM expenses;
ALTER TABLE expenses AUTO_INCREMENT = 1;
```

---

## 📈 Analytics Queries

### Top 5 Spending Categories
```sql
SELECT category, SUM(amount) as total
FROM expenses
GROUP BY category
ORDER BY total DESC
LIMIT 5;
```

### Daily Spending This Month
```sql
SELECT date, SUM(amount) as daily_total
FROM expenses
WHERE YEAR(date) = YEAR(CURDATE()) 
  AND MONTH(date) = MONTH(CURDATE())
GROUP BY date
ORDER BY date;
```

### Average Expense by Category
```sql
SELECT category, 
       COUNT(*) as count,
       AVG(amount) as average,
       MIN(amount) as minimum,
       MAX(amount) as maximum
FROM expenses
GROUP BY category;
```

### Compare This Month vs Last Month
```sql
SELECT 
    'This Month' as period,
    SUM(amount) as total
FROM expenses
WHERE date >= DATE_FORMAT(CURDATE(), '%Y-%m-01')

UNION ALL

SELECT 
    'Last Month' as period,
    SUM(amount) as total
FROM expenses
WHERE date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01')
  AND date < DATE_FORMAT(CURDATE(), '%Y-%m-01');
```

---

## 🏷️ Categories

### View All Categories
```sql
SELECT * FROM categories ORDER BY name;
```

### Add New Category
```sql
INSERT INTO categories (name, description)
VALUES ('Fitness', 'Gym membership, sports equipment, fitness classes');
```

---

## 🐛 Troubleshooting

### Connection Issues
```bash
# Check MySQL is running
brew services list  # macOS
sudo systemctl status mysql  # Linux
net start MySQL  # Windows

# Test connection
mysql -u expense_user -p -e "SELECT 1"
```

### Reset Password
```sql
ALTER USER 'expense_user'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

### Check Table Structure
```sql
DESCRIBE expenses;
SHOW CREATE TABLE expenses;
```

### View Indexes
```sql
SHOW INDEXES FROM expenses;
```

---

## 💡 Tips

- Use `LIMIT` for large result sets
- Always backup before making schema changes
- Test queries on sample data first
- Use prepared statements in application code
- Monitor slow queries with `EXPLAIN`

---

**Last Updated:** November 3, 2025
