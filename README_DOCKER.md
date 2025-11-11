# Smart Expense Tracker - Docker Setup for Teams
# UMGC CMSC 495 Capstone Project - Group 3

## 🐋 Why Docker for Group Projects?

✅ **Consistency** - Everyone runs identical environment  
✅ **No MySQL Installation** - Just install Docker once  
✅ **Quick Setup** - One command starts everything  
✅ **Easy Sharing** - Commit docker-compose.yml to Git  
✅ **Clean Environment** - Isolated from other projects  
✅ **Cross-Platform** - Works on Windows, Mac, Linux  

---

## 📋 Prerequisites

### One-Time Setup for Team Members

1. **Install Docker Desktop**
   - Windows/Mac: https://www.docker.com/products/docker-desktop/
   - Linux: https://docs.docker.com/engine/install/

2. **Verify Installation**
   ```powershell
   docker --version
   docker-compose --version
   ```

That's it! No MySQL installation needed.

---

## 🚀 Quick Start Guide

### For the Whole Team - Same Commands!

### Step 1: Start MySQL Database

```powershell
# Navigate to project directory
cd "c:\Users\EricG\OneDrive\Desktop\CAPSTONE PROJ\integration testing frontend+backend\SmartExpenseTrackingApp-1"

# Start MySQL with Docker Compose (recommended)
docker-compose up -d

# Or use the quick start script
.\quick-start.bat
# Choose option 3
```

**What this does:**
- Downloads MySQL 8.0 image (first time only)
- Creates `expense_db` database
- Creates `expense_user` with password
- Initializes database schema automatically
- Runs MySQL on port 3306

**Output should show:**
```
✓ Container expense-tracker-mysql  Started
```

### Step 2: Verify MySQL is Running

```powershell
# Check container status
docker ps

# Should show:
# CONTAINER ID   IMAGE       PORTS                    STATUS
# xxxxxxxxxxxx   mysql:8.0   0.0.0.0:3306->3306/tcp   Up X seconds
```

### Step 3: Start Backend

```powershell
# Backend connects to MySQL automatically
.\mvnw.cmd spring-boot:run
```

**Look for:**
```
✓ Started ExpenseApiApplication in X.XXX seconds
✓ HikariPool-1 - Start completed
```

### Step 4: Test Integration

```powershell
# Open new PowerShell window
.\test-integration.ps1
```

**Or manually test frontend:**
1. Open `frontend/index.html` in browser
2. Add an expense
3. Refresh page - data persists! ✨

---

## 🎯 Docker Commands Cheat Sheet

### Starting & Stopping

```powershell
# Start MySQL (creates and starts container)
docker-compose up -d

# Stop MySQL (keeps data)
docker-compose stop

# Start again after stopping
docker-compose start

# Stop and remove containers (keeps data in volume)
docker-compose down

# Stop and remove everything including data ⚠️
docker-compose down -v
```

### Checking Status

```powershell
# View running containers
docker ps

# View all containers (including stopped)
docker ps -a

# View logs
docker-compose logs

# Follow logs in real-time
docker-compose logs -f

# View MySQL logs specifically
docker logs expense-tracker-mysql
```

### Database Access

```powershell
# Connect to MySQL CLI
docker exec -it expense-tracker-mysql mysql -u expense_user -pexpense_password expense_db

# Run SQL query directly
docker exec -it expense-tracker-mysql mysql -u expense_user -pexpense_password expense_db -e "SELECT * FROM expenses;"

# Run SQL script
docker exec -i expense-tracker-mysql mysql -u expense_user -pexpense_password expense_db < your-script.sql
```

### Troubleshooting

```powershell
# Restart MySQL container
docker-compose restart

# View detailed container info
docker inspect expense-tracker-mysql

# Check container health
docker-compose ps

# Remove and recreate (if something is wrong)
docker-compose down
docker-compose up -d
```

---

## 📊 Understanding Docker Compose Setup

Our `docker-compose.yml` configures:

```yaml
MySQL Container:
├── Image: mysql:8.0 (official)
├── Port: 3306 (accessible from host)
├── Database: expense_db (auto-created)
├── User: expense_user / expense_password
├── Schema: Auto-loaded from database/schema.sql
├── Data: Persisted in Docker volume
└── Network: expense-tracker-network
```

**Benefits:**
- ✅ Database survives container restarts
- ✅ Schema automatically initialized
- ✅ Same setup across all team member machines
- ✅ Easy to reset for testing

---

## 👥 Team Workflow

### For Each Team Member (First Time):

1. **Clone the repository**
   ```powershell
   git clone https://github.com/dmitc072/SmartExpenseTrackingApp.git
   cd SmartExpenseTrackingApp
   ```

2. **Install Docker Desktop**
   - Download and install from docker.com
   - Start Docker Desktop

3. **Start the stack**
   ```powershell
   docker-compose up -d
   .\mvnw.cmd spring-boot:run
   ```

4. **Test it works**
   ```powershell
   .\test-integration.ps1
   ```

### Daily Development:

```powershell
# Morning - Start work
docker-compose start              # Start MySQL
.\mvnw.cmd spring-boot:run       # Start backend

# During day - Test changes
.\test-integration.ps1           # Run tests

# Evening - Stop work
# Ctrl+C in backend terminal
docker-compose stop              # Stop MySQL (keeps data)
```

### Sharing Schema Changes:

```powershell
# If database schema changes:
# 1. Update database/schema.sql
# 2. Commit to Git
# 3. Team members run:
docker-compose down -v           # Remove old data
docker-compose up -d             # Recreate with new schema
```

---

## 🔄 Data Persistence

### Where is data stored?

```powershell
# Docker volume (persists across restarts)
docker volume ls
# Shows: mysql-data

# Inspect volume
docker volume inspect mysql-data
```

### Reset database to fresh state:

```powershell
# Remove all data and recreate
docker-compose down -v
docker-compose up -d
# Schema automatically reloaded from database/schema.sql
```

### Backup database:

```powershell
# Export all data
docker exec expense-tracker-mysql mysqldump -u expense_user -pexpense_password expense_db > backup.sql

# Restore from backup
docker exec -i expense-tracker-mysql mysql -u expense_user -pexpense_password expense_db < backup.sql
```

---

## 🐛 Troubleshooting

### Issue: "Port 3306 already in use"

**Cause:** Another MySQL instance running

**Solution:**
```powershell
# Option 1: Stop other MySQL
# - XAMPP: Stop MySQL in control panel
# - Windows Service: Stop MySQL service

# Option 2: Change Docker port in docker-compose.yml
# Change "3306:3306" to "3307:3306"
# Update application.properties: localhost:3307
```

### Issue: "Cannot connect to Docker daemon"

**Cause:** Docker Desktop not running

**Solution:**
1. Start Docker Desktop
2. Wait for "Docker Desktop is running" in system tray
3. Try command again

### Issue: "Container keeps restarting"

**Check logs:**
```powershell
docker logs expense-tracker-mysql

# Common causes:
# - Port already in use
# - Insufficient memory
# - Corrupted data volume
```

**Fix:**
```powershell
# Remove and recreate
docker-compose down -v
docker-compose up -d
```

### Issue: "Schema not initialized"

**Verify schema file:**
```powershell
# Check if schema.sql is in correct location
ls database\schema.sql

# Recreate container to reload schema
docker-compose down
docker-compose up -d
```

---

## 🎓 For Team Demos / Presentations

### Quick Demo Setup (Clean State):

```powershell
# 1. Start with fresh database
docker-compose down -v
docker-compose up -d

# 2. Wait for MySQL to be ready (15 seconds)
timeout /t 15 /nobreak

# 3. Start backend
.\mvnw.cmd spring-boot:run

# 4. Open frontend
start frontend\index.html

# 5. Demo to client/professor
# Everything is clean and ready!
```

### Add Sample Data for Demo:

```powershell
# Create sample-data.sql in database folder
docker exec -i expense-tracker-mysql mysql -u expense_user -pexpense_password expense_db < database\sample-data.sql
```

---

## 📦 Alternative: Docker for Backend Too

Want to containerize the **entire stack**? 

### Create Backend Dockerfile:

```dockerfile
# Dockerfile.backend
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/expense-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Update docker-compose.yml:

Uncomment the `backend` service section in `docker-compose.yml`

### Run everything with one command:

```powershell
# Build and start everything
docker-compose up -d --build

# Frontend still accessed via browser (frontend/index.html)
# Backend API: http://localhost:8080
# MySQL: localhost:3306
```

**Benefits:**
- ✅ Entire stack in Docker
- ✅ Even easier for team members
- ✅ Production-like environment

**Trade-offs:**
- ⚠️ Slightly slower development (need to rebuild on code changes)
- ⚠️ More complex debugging

---

## 📝 What to Commit to Git

✅ **DO commit:**
- `docker-compose.yml`
- `Dockerfile.mysql`
- `database/schema.sql`
- `README_DOCKER.md` (this file)

❌ **DON'T commit:**
- Docker volumes
- Container logs
- `.env` files with secrets (use `.env.example` instead)

### .gitignore additions:

```gitignore
# Docker
.env
docker-compose.override.yml
```

---

## 🎯 Success Criteria

Your Docker setup is working correctly if:

1. ✅ `docker ps` shows mysql container running
2. ✅ Backend starts without database connection errors
3. ✅ `.\test-integration.ps1` passes all tests
4. ✅ Frontend can CRUD expenses
5. ✅ Data persists after `docker-compose restart`
6. ✅ All team members can run same commands

---

## 🚀 Next Steps

### For Your Team:

1. **Share this guide** with all team members
2. **Add to Git:** Commit docker files
3. **Team meeting:** Everyone runs `docker-compose up -d` together
4. **Test together:** Everyone runs integration tests
5. **Develop:** Work on features with confidence

### Production Deployment:

When ready to deploy:
- Use managed MySQL (AWS RDS, Azure Database)
- Or deploy with Docker Swarm / Kubernetes
- Configure proper secrets management
- Set up CI/CD with Docker builds

---

## 💡 Pro Tips

1. **Always use `docker-compose`** instead of raw `docker run` commands
2. **Stop containers when not working** to save resources
3. **Use volumes for data** - already configured in docker-compose.yml
4. **Share docker-compose.yml** in Git - everyone uses same config
5. **Document any custom changes** in this file

---

## 📞 Help

**Docker not working?**
1. Check Docker Desktop is running
2. Check logs: `docker-compose logs`
3. Restart: `docker-compose restart`
4. Fresh start: `docker-compose down -v && docker-compose up -d`

**Team member issues?**
- Ensure Docker Desktop installed
- Ensure Git pulled latest changes
- Try clean start: `docker-compose down -v && docker-compose up -d`

---

## Summary

**For your group project, Docker provides:**

✅ Identical setup across Windows/Mac/Linux  
✅ No "works on my machine" problems  
✅ Quick onboarding for new team members  
✅ Easy cleanup and reset  
✅ Production-like environment  
✅ Simple commands everyone can use  

**One command to rule them all:**
```powershell
docker-compose up -d
```

**You're ready! 🎉**
