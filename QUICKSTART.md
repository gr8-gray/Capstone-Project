# 🚀 Quick Start Guide

Get the Smart Expense Tracker running in under 5 minutes!

## Prerequisites

- Docker Desktop installed ([Download](https://www.docker.com/products/docker-desktop/))
- Java 17+ installed
- Git installed

## Setup Steps

### 1. Clone Repository
```powershell
git clone https://github.com/dmitc072/SmartExpenseTrackingApp.git
cd SmartExpenseTrackingApp
```

### 2. Start MySQL Database
```powershell
docker compose up -d
```
Wait 15-20 seconds for MySQL to initialize and become healthy.

### 3. Start Backend Server
```powershell
.\mvnw.cmd spring-boot:run
```
Wait for: `Started ExpenseApiApplication in X.XXX seconds`

Keep this terminal open!

### 4. Open Frontend
```powershell
start frontend\dashboard.html
```

## ✅ You're Done!

- Add an expense in the frontend
- Refresh the page - your data persists!

## 🔧 Troubleshooting

**Backend won't start?**
- Make sure Docker MySQL is running: `docker ps`
- Check logs: `docker compose logs`
- Verify MySQL is healthy (status should show "healthy")

**Frontend not connecting?**
- Verify backend is running on http://localhost:8080
- Check browser console for errors (F12)
- Ensure CORS is enabled in backend (already configured)

**Docker issues?**
- Make sure Docker Desktop is running
- Test: `docker --version` and `docker ps`

## 📚 Need More Help?

- Full Docker guide: [README_DOCKER.md](README_DOCKER.md)
- Team onboarding: [TEAM_ONBOARDING.md](TEAM_ONBOARDING.md)
- Main documentation: [README.md](README.md)
