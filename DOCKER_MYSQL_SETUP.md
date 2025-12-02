# Docker MySQL Setup for Smart Expense Tracker

## Overview
The application is now configured to **always use MySQL running in Docker**. The H2 in-memory database is only used for tests.

## Prerequisites

1. **Docker Desktop** must be installed and running
   - Download from: https://www.docker.com/products/docker-desktop/
   - Make sure Docker Desktop is running before starting the application

## Quick Start

### Option 1: Use the Startup Script (Recommended)

```powershell
.\start-with-docker.ps1
```

This script will:
1. Check if Docker is running
2. Start MySQL container
3. Wait for MySQL to be healthy
4. Start Spring Boot application

### Option 2: Manual Steps

1. **Start MySQL container:**
   ```powershell
   docker compose up -d
   ```

2. **Wait 15-20 seconds** for MySQL to initialize

3. **Start the backend:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

## Stopping the Application

### Stop Everything:
```powershell
.\stop-docker.ps1
```

Or manually:
```powershell
# Stop Spring Boot (Ctrl+C in the terminal where it's running)
# Then stop MySQL:
docker compose down
```

## Database Configuration

### MySQL Connection Details:
- **Host**: localhost
- **Port**: 3306
- **Database**: expense_db
- **Username**: expense_user
- **Password**: expense_password

### Container Name:
- `smartexpensetrackingapp-mysql-1`

## Verifying MySQL is Running

```powershell
# Check container status
docker ps

# Check MySQL health
docker inspect --format='{{.State.Health.Status}}' smartexpensetrackingapp-mysql-1

# View MySQL logs
docker compose logs mysql

# Connect to MySQL CLI
docker exec -it smartexpensetrackingapp-mysql-1 mysql -u expense_user -p
# Enter password: expense_password
```

## Data Persistence

- Database data is stored in a Docker volume named `mysql_data`
- Data persists between container restarts
- To completely reset the database:
  ```powershell
  docker compose down -v
  docker compose up -d
  ```

## Configuration Changes Made

### 1. `application.properties`
- Changed from H2 to MySQL connection
- Updated Hibernate dialect to MySQL
- Changed `ddl-auto` from `create-drop` to `update` (preserves data)

### 2. `pom.xml`
- Enabled MySQL connector dependency
- H2 is now only available for tests

### 3. `SecurityConfig.java`
- Removed H2 console endpoints (not needed for MySQL)

## Troubleshooting

### Issue: "Docker is not running"
**Solution**: Start Docker Desktop application

### Issue: "Failed to start MySQL container"
**Solution**: 
- Check if port 3306 is already in use
- Try: `docker compose down` then `docker compose up -d`

### Issue: "Access denied for user 'expense_user'"
**Solution**: 
- Stop containers: `docker compose down`
- Remove volumes: `docker volume rm smartexpensetrackingapp_mysql_data`
- Start again: `docker compose up -d`

### Issue: Connection timeout
**Solution**:
- Make sure MySQL container is healthy: `docker ps`
- Wait longer for MySQL to initialize (first start takes 30-60 seconds)
- Check logs: `docker compose logs mysql`

## Database Schema

The application automatically creates/updates tables:
- `users` - User accounts
- `expenses` - Expense records
- `budgets` - Budget tracking
- `categories` - Expense categories

## Development vs Production

### Development (Current Setup):
- Docker MySQL on localhost
- `spring.jpa.hibernate.ddl-auto=update` (auto-creates tables)
- Debug logging enabled

### Production (Recommended):
- Use managed MySQL service (AWS RDS, Azure Database, etc.)
- `spring.jpa.hibernate.ddl-auto=validate` (no auto-changes)
- Minimal logging
- Use environment variables for credentials

## Testing

Tests still use H2 in-memory database for speed and isolation:
- Test configuration in `src/test/resources/application-test.properties`
- H2 dependency scope is `test` only

## Benefits of Docker MySQL

✅ **Data Persistence** - Data survives application restarts  
✅ **Production-like** - Tests against real MySQL database  
✅ **Team Consistency** - Everyone uses same database version  
✅ **Isolation** - Database runs in container, doesn't affect system  
✅ **Easy Reset** - Can easily wipe and recreate database  

## Next Steps

1. **Start the application:**
   ```powershell
   .\start-with-docker.ps1
   ```

2. **Open frontend**: `frontend/index.html`

3. **Register and use the app** - Data will persist!

4. **Stop when done:**
   ```powershell
   .\stop-docker.ps1
   ```
