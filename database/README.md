# Smart Expense Tracker - Database Setup Guide

**Database Engineer:** Michael Basye  
**UMGC CMSC 495 Capstone Project - Group 3**

---

## 📋 Prerequisites

Before setting up the database, ensure you have:

- MySQL 8.0 or higher installed
- MySQL running on localhost (default port 3306)
- Root access to MySQL server

### Install MySQL on macOS

```bash
# Using Homebrew
brew install mysql

# Start MySQL service
brew services start mysql

# Secure installation (set root password)
mysql_secure_installation
```

### Install MySQL on Windows

#### Option 1: Using MySQL Installer (Recommended)
1. Download MySQL installer from [mysql.com](https://dev.mysql.com/downloads/installer/)
2. Run `mysql-installer-community-x.x.x.msi`
3. Choose "Developer Default" or "Server only" setup type
4. Set root password during configuration
5. Configure MySQL as a Windows Service (auto-start)
6. Complete installation and note the installation path

#### Option 2: Using Chocolatey (Package Manager)
```powershell
# Run PowerShell as Administrator
choco install mysql

# Start MySQL service
net start MySQL

# Secure installation
mysql_secure_installation
```

#### Option 3: Using Scoop
```powershell
scoop install mysql
mysqld --install
net start MySQL
```

**Note for Windows users:** Add MySQL to your PATH:
- Default location: `C:\Program Files\MySQL\MySQL Server 8.x\bin`
- Add to System Environment Variables → Path

### Install MySQL on Linux

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql  # Auto-start on boot
sudo mysql_secure_installation
```

#### Fedora/RHEL/CentOS
```bash
sudo dnf install mysql-server  # Fedora
# OR
sudo yum install mysql-server  # RHEL/CentOS

sudo systemctl start mysqld
sudo systemctl enable mysqld
sudo mysql_secure_installation
```

#### Arch Linux
```bash
sudo pacman -S mysql
sudo mysqld --initialize --user=mysql
sudo systemctl start mysqld
sudo systemctl enable mysqld
sudo mysql_secure_installation
```

---

## 🚀 Quick Setup

### Option 1: Automated Setup (Recommended)

Run the provided setup script:

#### macOS/Linux
```bash
# From the project root directory
cd database

# Run setup (will prompt for MySQL root password)
mysql -u root -p < setup.sql
mysql -u root -p < schema.sql
```

#### Windows (Command Prompt)
```cmd
cd database
mysql -u root -p < setup.sql
mysql -u root -p < schema.sql
```

#### Windows (PowerShell)
```powershell
cd database
Get-Content setup.sql | mysql -u root -p
Get-Content schema.sql | mysql -u root -p
```

### Option 2: Manual Setup

#### Step 1: Create User
```bash
mysql -u root -p
```

```sql
CREATE USER 'expense_user'@'localhost' IDENTIFIED BY 'expense_password';
FLUSH PRIVILEGES;
EXIT;
```

#### Step 2: Create Database and Schema
```bash
mysql -u root -p < schema.sql
```

#### Step 3: Grant Privileges
```bash
mysql -u root -p < setup.sql
```

---

## 📊 Database Structure

### Tables

| Table Name   | Purpose                          | Status       |
|-------------|----------------------------------|--------------|
| `expenses`   | Core expense tracking data       | ✅ Active    |
| `categories` | Predefined expense categories    | ✅ Active    |
| `users`      | User authentication (Phase 2)    | 📝 Prepared  |
| `budgets`    | Budget tracking (future)         | 📝 Prepared  |

### Views

- `v_monthly_expenses` - Monthly spending summary by category
- `v_category_totals` - Total spending by category
- `v_recent_expenses` - Expenses from last 30 days

### Stored Procedures

- `sp_get_monthly_summary(year, month)` - Get monthly breakdown
- `sp_get_expenses_by_date_range(start, end)` - Filter by date
- `sp_budget_status(year, month)` - Check budget vs spending

---

## 🧪 Verify Installation

### Check Database Creation
```bash
mysql -u expense_user -p expense_db
```

```sql
-- Should show: expense_db
SHOW DATABASES;

-- Should show: expenses, categories, users, budgets
SHOW TABLES;

-- Should show 16 categories
SELECT COUNT(*) FROM categories;

-- Should show sample expenses
SELECT * FROM expenses;
```

### Test Connection from Application

#### macOS/Linux
```bash
# From project root
./mvnw spring-boot:run
```

#### Windows (Command Prompt)
```cmd
mvnw.cmd spring-boot:run
```

#### Windows (PowerShell)
```powershell
.\mvnw.cmd spring-boot:run
```

Check logs for:
```
HikariPool-1 - Start completed.
Hibernate: ...
```

---

## 🔧 Configuration

The application expects these database credentials (from `application.properties`):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_db
spring.datasource.username=expense_user
spring.datasource.password=expense_password
```

### Change Database Password

```sql
ALTER USER 'expense_user'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

Then update `src/main/resources/application.properties`:
```properties
spring.datasource.password=new_password
```

---

## 📝 Sample Data

The schema includes sample expense data for testing:

| Description                   | Amount  | Category           | Date       |
|------------------------------|---------|-------------------|------------|
| Grocery shopping             | $125.50 | Food & Dining     | 2025-11-01 |
| Gas station fill-up          | $45.00  | Transportation    | 2025-11-01 |
| Netflix subscription         | $15.99  | Subscriptions     | 2025-11-01 |
| Lunch at cafe                | $18.75  | Food & Dining     | 2025-11-02 |
| Electric bill payment        | $89.50  | Bills & Utilities | 2025-11-02 |

**Remove sample data for production:**
```sql
DELETE FROM expenses;
ALTER TABLE expenses AUTO_INCREMENT = 1;
```

---

## 🛠 Common Operations

### Backup Database
```bash
mysqldump -u expense_user -p expense_db > backup_$(date +%Y%m%d).sql
```

### Restore Database
```bash
mysql -u expense_user -p expense_db < backup_20251103.sql
```

### Reset Database
```bash
mysql -u root -p
```
```sql
DROP DATABASE expense_db;
SOURCE schema.sql;
```

### View Database Size
```sql
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'expense_db'
GROUP BY table_schema;
```

---

## 🔍 Troubleshooting

### Cannot Connect to MySQL

#### macOS
```bash
brew services list
brew services restart mysql
```

#### Linux
```bash
sudo systemctl status mysql
sudo systemctl restart mysql
```

#### Windows
```cmd
# Check service status
sc query MySQL

# Start service
net start MySQL

# Restart service
net stop MySQL
net start MySQL
```

Or use Windows Services GUI (`services.msc`)

### Authentication Failed
```sql
-- Reset user password
ALTER USER 'expense_user'@'localhost' IDENTIFIED BY 'expense_password';
FLUSH PRIVILEGES;
```

### Port 3306 Already in Use

#### macOS/Linux
```bash
# Check what's using port 3306
lsof -i :3306

# Kill process if needed
sudo kill -9 <PID>
```

#### Windows
```cmd
# Check what's using port 3306
netstat -ano | findstr :3306

# Kill process if needed
taskkill /PID <PID> /F
```

#### Change MySQL Port (All Platforms)

**macOS/Linux:** Edit `/etc/my.cnf` or `/etc/mysql/my.cnf`
```ini
[mysqld]
port=3307
```

**Windows:** Edit `C:\ProgramData\MySQL\MySQL Server 8.x\my.ini`
```ini
[mysqld]
port=3307
```

Then update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3307/expense_db
```

### Application Cannot Find Database
1. Verify database exists: `SHOW DATABASES;`
2. Verify user has access: `SHOW GRANTS FOR 'expense_user'@'localhost';`
3. Check application.properties configuration
4. Ensure MySQL is running

---

## 📚 SQL Query Examples

### Get Monthly Spending
```sql
CALL sp_get_monthly_summary(2025, 11);
```

### Get Expenses by Date Range
```sql
CALL sp_get_expenses_by_date_range('2025-11-01', '2025-11-30');
```

### Top Spending Categories
```sql
SELECT * FROM v_category_totals LIMIT 5;
```

### Add New Expense
```sql
INSERT INTO expenses (description, amount, category, date)
VALUES ('Coffee at Starbucks', 5.75, 'Food & Dining', CURDATE());
```

### Monthly Category Breakdown
```sql
SELECT * FROM v_monthly_expenses 
WHERE year = 2025 AND month = 11
ORDER BY total_amount DESC;
```

---

## 🔐 Security Notes

### For Production Deployment:

1. **Change default password:**
   ```sql
   ALTER USER 'expense_user'@'localhost' IDENTIFIED BY 'STRONG_RANDOM_PASSWORD';
   ```

2. **Restrict privileges:**
   ```sql
   REVOKE ALL ON expense_db.* FROM 'expense_user'@'localhost';
   GRANT SELECT, INSERT, UPDATE, DELETE ON expense_db.* TO 'expense_user'@'localhost';
   ```

3. **Enable SSL/TLS:**
   - Configure MySQL to require SSL connections
   - Update application.properties with SSL parameters

4. **Remove sample data:**
   ```sql
   DELETE FROM expenses;
   ```

---

## 📞 Support

For database-related issues, contact:
- **Michael Basye** - Database Engineer
- Team Discord/Teams channel
- Create GitHub issue with `database` label

---

## 📋 Checklist for Team Members

- [ ] MySQL installed and running
- [ ] Root password set and saved securely
- [ ] Ran `setup.sql` successfully
- [ ] Ran `schema.sql` successfully
- [ ] Verified 16 categories exist
- [ ] Can connect with `expense_user` credentials
- [ ] Application starts without database errors
- [ ] Can create/read/update/delete expenses via API

---

**Last Updated:** November 3, 2025  
**Schema Version:** 1.0
