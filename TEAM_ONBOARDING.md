# 🚀 Team Member Onboarding - 5 Minute Setup

**Welcome to Smart Expense Tracker - Group 3!**

Follow these steps to get your development environment ready.

---

## ✅ Prerequisites (Install Once)

### 1. Git
- Download: https://git-scm.com/downloads
- Verify: `git --version`

### 2. Java 17+
- Download: https://adoptium.net/ (choose JDK 17 or later)
- Verify: `java -version`

### 3. Docker Desktop 🐋 (Recommended)
- Download: https://www.docker.com/products/docker-desktop/
- Install and start Docker Desktop
- Verify: `docker --version`

---

## 🎯 Setup Your Environment (First Time)

### Step 1: Clone the Repository

```powershell
# Navigate to where you want the project
cd "C:\Users\YourName\Desktop"

# Clone the repo
git clone https://github.com/dmitc072/SmartExpenseTrackingApp.git

# Enter the project
cd SmartExpenseTrackingApp
```

### Step 2: Start MySQL Database

```powershell
# Using Docker (Recommended - Same for everyone!)
docker-compose up -d

# Or use the setup script
.\setup-docker.ps1
# Choose option 1

# Wait 15 seconds for MySQL to initialize
```

**✅ Success indicators:**
- `Container expense-tracker-mysql Started`
- No error messages

### Step 3: Start Backend Server

```powershell
# Start Spring Boot application
.\mvnw.cmd spring-boot:run
```

**✅ Wait for:**
- `Started ExpenseApiApplication in X.XXX seconds`
- `Tomcat started on port(s): 8080`

**⚠️ Keep this terminal open!**

### Step 4: Test Everything Works

Open a **NEW PowerShell window**:

```powershell
# Navigate to project
cd SmartExpenseTrackingApp

# Run automated tests
.\test-integration.ps1
```

**✅ Success:** Should see "ALL TESTS PASSED"

### Step 5: Open Frontend

```powershell
# Open in browser
start frontend\index.html

# Or double-click frontend/index.html in File Explorer
```

**Test it:**
1. Add an expense
2. Refresh the page
3. Expense should still be there! ✨

---

## 🎓 Daily Development Workflow

### Morning - Start Development

```powershell
# 1. Pull latest changes
git pull origin main

# 2. Start MySQL (if not running)
docker-compose start

# 3. Start backend
.\mvnw.cmd spring-boot:run
```

### During Day - Make Changes

```powershell
# After making code changes:

# 1. Stop backend (Ctrl+C)
# 2. Restart backend
.\mvnw.cmd spring-boot:run

# 3. Test your changes
.\test-integration.ps1
```

### Evening - End Development

```powershell
# 1. Stop backend (Ctrl+C in backend terminal)

# 2. Stop MySQL (saves resources)
docker-compose stop

# 3. Commit your changes
git add .
git commit -m "Your descriptive message"
git push origin your-branch-name
```

---

## 🔧 Common Commands

### Git Commands

```powershell
# Get latest changes
git pull origin main

# Create new branch for your feature
git checkout -b feature/your-feature-name

# Check what changed
git status
git diff

# Commit changes
git add .
git commit -m "Description of changes"

# Push to GitHub
git push origin feature/your-feature-name
```

### Docker Commands

```powershell
# Start MySQL
docker-compose up -d

# Stop MySQL (keeps data)
docker-compose stop

# Start again
docker-compose start

# View logs
docker-compose logs

# Access MySQL CLI
docker exec -it expense-tracker-mysql mysql -u expense_user -pexpense_password expense_db

# Reset everything (fresh start)
docker-compose down -v
docker-compose up -d
```

### Maven Commands

```powershell
# Start backend
.\mvnw.cmd spring-boot:run

# Build project
.\mvnw.cmd clean package

# Run tests
.\mvnw.cmd test

# Clean build artifacts
.\mvnw.cmd clean
```

### Testing Commands

```powershell
# Run integration tests
.\test-integration.ps1

# Quick start menu
.\quick-start.bat

# Docker setup menu
.\setup-docker.ps1
```

---

## 🐛 Troubleshooting

### Issue: "Cannot connect to Docker daemon"

**Fix:**
1. Open Docker Desktop
2. Wait for "Docker Desktop is running"
3. Try command again

### Issue: "Port 8080 already in use"

**Fix:**
```powershell
# Find what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace XXXX with PID from above)
taskkill /F /PID XXXX

# Or restart backend
```

### Issue: "Port 3306 already in use"

**Fix:**
```powershell
# Stop other MySQL (if XAMPP or other MySQL installed)
# Then start Docker MySQL:
docker-compose up -d
```

### Issue: "mvnw.cmd not found"

**Fix:**
```powershell
# Make sure you're in the project root directory
cd SmartExpenseTrackingApp

# Verify mvnw.cmd exists
ls mvnw.cmd
```

### Issue: "Failed to load expenses from server"

**Fix:**
1. Backend not running → Start with `.\mvnw.cmd spring-boot:run`
2. Check backend logs for errors
3. Test API: `curl http://localhost:8080/api/expenses`

### Issue: Frontend not updating

**Fix:**
1. Hard refresh browser (Ctrl+F5)
2. Clear browser cache
3. Check browser console (F12) for errors

---

## 📁 Project Structure

```
SmartExpenseTrackingApp/
├── frontend/               # HTML/CSS/JavaScript
│   ├── index.html         # Login page
│   ├── dashboard.html     # Main app
│   ├── script.js          # Frontend logic
│   └── style.css          # Styling
├── src/                   # Java backend
│   ├── main/
│   │   ├── java/          # Java source code
│   │   └── resources/     # application.properties
│   └── test/              # Test files
├── database/              # Database files
│   └── schema.sql         # Database structure
├── docker-compose.yml     # Docker configuration
├── pom.xml               # Maven dependencies
├── mvnw.cmd              # Maven wrapper
└── README files          # Documentation
```

---

## 🎯 Your First Task

Try this to verify everything works:

1. ✅ Start MySQL: `docker-compose up -d`
2. ✅ Start backend: `.\mvnw.cmd spring-boot:run`
3. ✅ Run tests: `.\test-integration.ps1`
4. ✅ Open frontend: `start frontend\index.html`
5. ✅ Add an expense in the UI
6. ✅ Refresh page - expense still there
7. ✅ Check database:
   ```powershell
   docker exec -it expense-tracker-mysql mysql -u expense_user -pexpense_password expense_db -e "SELECT * FROM expenses;"
   ```

**If all steps work, you're ready to develop! 🎉**

---

## 📚 Documentation

- **Quick Start:** [README_INTEGRATION.md](README_INTEGRATION.md)
- **Docker Guide:** [README_DOCKER.md](README_DOCKER.md)
- **Detailed Testing:** [INTEGRATION_TEST_GUIDE.md](INTEGRATION_TEST_GUIDE.md)
- **Architecture:** [ARCHITECTURE.md](ARCHITECTURE.md)
- **MySQL Manual Setup:** [MYSQL_SETUP.md](MYSQL_SETUP.md)

---

## 👥 Team Communication

**Before starting work:**
- Check GitHub Issues for assigned tasks
- Pull latest changes: `git pull origin main`
- Create feature branch: `git checkout -b feature/your-task`

**While working:**
- Commit often with clear messages
- Push to your feature branch
- Test before pushing: `.\test-integration.ps1`

**When done:**
- Push your branch: `git push origin feature/your-task`
- Create Pull Request on GitHub
- Request code review from team

---

## 🆘 Getting Help

1. **Check documentation** in README files
2. **Check Docker status:** `.\setup-docker.ps1` (choose option 2)
3. **Check backend logs** in terminal where backend is running
4. **Check browser console** (F12 → Console tab)
5. **Ask team** in your group chat or Slack/Discord
6. **Create GitHub Issue** for bugs or problems

---

## ✨ Tips for Success

1. **Pull before you start** - Always get latest code
2. **Test before you commit** - Run `.\test-integration.ps1`
3. **Use feature branches** - Don't work directly on main
4. **Write clear commit messages** - "Fixed login bug" not "fixes"
5. **Keep Docker running** - Faster development
6. **Document your changes** - Update README if you add features

---

## 🎊 You're Ready!

Your development environment is set up. Now you can:

- ✅ Develop new features
- ✅ Fix bugs
- ✅ Run tests
- ✅ Collaborate with team
- ✅ Deploy to production (later)

**Happy coding! 🚀**

---

**Questions?** Ask in the team chat or check the documentation files.

**Last Updated:** November 9, 2025
