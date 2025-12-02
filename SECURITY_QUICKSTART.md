# Security Configuration Quick Start Guide

## 🚀 Quick Setup for Development

### 1. Create Your Environment File
```bash
# Copy the template
cp .env.example .env
```

### 2. Generate a Strong JWT Secret
```bash
# On Linux/Mac
openssl rand -base64 64

# On Windows PowerShell
$bytes = New-Object byte[] 64; (New-Object Security.Cryptography.RNGCryptoServiceProvider).GetBytes($bytes); [Convert]::ToBase64String($bytes)
```

### 3. Edit `.env` File
Update the following values:
```env
DB_URL=jdbc:mysql://localhost:3306/expensetracker
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=paste_generated_secret_here
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

---

## ⚠️ IMPORTANT: What Changed

### Before (INSECURE ❌)
```properties
spring.datasource.password=lWQpawvCywrdTDhTYUTdhauZYHnVLWnj
app.jwt.secret=mySecretKeyForJWTToken...
```

### After (SECURE ✅)
```properties
spring.datasource.password=${DB_PASSWORD}
app.jwt.secret=${JWT_SECRET}
```

---

## 🔐 Security Rules

### ❌ NEVER DO THIS:
- ❌ Commit `.env` file to Git
- ❌ Share secrets in Slack/Email/Discord
- ❌ Use default passwords in production
- ❌ Enable DEBUG logging in production
- ❌ Hardcode credentials in code

### ✅ ALWAYS DO THIS:
- ✅ Use environment variables for secrets
- ✅ Rotate secrets regularly
- ✅ Use strong, unique passwords
- ✅ Enable INFO/WARN logging in production
- ✅ Keep `.env.example` updated

---

## 🌍 Environment Profiles

### Development (default)
```bash
mvn spring-boot:run
```

### Production
```bash
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

Or with Docker:
```bash
docker-compose --env-file .env up
```

---

## 🐛 Troubleshooting

### Application Won't Start
```bash
# Check if .env file exists
ls -la .env

# Verify environment variables are loaded
env | grep DB_
env | grep JWT_
```

### Database Connection Fails
```bash
# Test connection manually
mysql -h localhost -u your_username -p
```

### JWT Token Invalid
- Ensure JWT_SECRET is at least 256 bits (43+ characters in base64)
- Verify secret matches across all instances
- Check token expiration time

---

## 📚 More Information

See `SECURITY_FIXES_SUMMARY.md` for complete documentation.

---

## 🆘 Need Help?

Contact the backend team:
- Eric Gray (Backend Developer)
- [Team Lead Name]
