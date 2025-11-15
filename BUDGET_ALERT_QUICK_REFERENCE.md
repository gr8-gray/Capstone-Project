# Budget Alert System - Quick Reference

## 🚀 Quick Start

### 1. Check All Budgets for Alerts
```bash
curl -X POST http://localhost:8080/api/budget-alerts/check-all \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Get Unread Alerts
```bash
curl -X GET http://localhost:8080/api/budget-alerts/unread \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Get Alert Count (for badge)
```bash
curl -X GET http://localhost:8080/api/budget-alerts/count/unread \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Mark Alert as Read
```bash
curl -X PATCH http://localhost:8080/api/budget-alerts/1/read \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📊 Alert Levels

| Level | % Threshold | Color | Icon |
|-------|-------------|-------|------|
| INFO | 50% | Blue | ℹ️ |
| WARNING | 75% | Yellow | ⚠️ |
| DANGER | 90% | Orange | 🔶 |
| CRITICAL | 100%+ | Red | 🚨 |

---

## 🔌 All API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/budget-alerts/check-all` | Check all budgets |
| POST | `/api/budget-alerts/check/{id}` | Check specific budget |
| GET | `/api/budget-alerts` | Get all alerts |
| GET | `/api/budget-alerts/unread` | Get unread alerts |
| GET | `/api/budget-alerts/critical` | Get critical alerts |
| GET | `/api/budget-alerts/budget/{id}` | Get alerts by budget |
| GET | `/api/budget-alerts/count/unread` | Get unread count |
| PATCH | `/api/budget-alerts/{id}/read` | Mark alert as read |
| PATCH | `/api/budget-alerts/read-all` | Mark all as read |

---

## 💾 Database Table

```sql
CREATE TABLE budget_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    budget_id BIGINT NOT NULL,
    alert_level VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    spent_amount DECIMAL(12, 2) NOT NULL,
    budget_limit DECIMAL(12, 2) NOT NULL,
    percentage_used DECIMAL(5, 2) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    read_at DATETIME
);
```

---

## 🧪 Testing Scenario

### Step 1: Create Budget ($500)
```json
POST /api/budgets
{
  "category": "Food & Dining",
  "limitAmount": 500.00,
  "startDate": "2025-11-01",
  "endDate": "2025-11-30"
}
```

### Step 2: Add Expense ($250 - 50%)
```json
POST /api/expenses
{
  "description": "Groceries",
  "amount": 250.00,
  "category": "Food & Dining",
  "date": "2025-11-15"
}
```

### Step 3: Check Budget
```bash
POST /api/budget-alerts/check-all
```
**Result**: INFO alert (50% reached)

### Step 4: Add More ($125 - now 75%)
```json
POST /api/expenses
{
  "amount": 125.00,
  "category": "Food & Dining",
  ...
}
```

### Step 5: Check Again
```bash
POST /api/budget-alerts/check-all
```
**Result**: WARNING alert (75% reached)

---

## 📱 Frontend Integration Snippets

### Display Alert Badge
```javascript
async function updateBadge() {
  const res = await fetch('/api/budget-alerts/count/unread');
  const {unreadCount} = await res.json();
  document.getElementById('badge').textContent = unreadCount;
}
```

### Load Alerts
```javascript
async function loadAlerts() {
  const res = await fetch('/api/budget-alerts/unread');
  const alerts = await res.json();
  
  alerts.forEach(alert => {
    console.log(`${alert.alertLevel}: ${alert.message}`);
  });
}
```

### Mark as Read
```javascript
async function markRead(id) {
  await fetch(`/api/budget-alerts/${id}/read`, {
    method: 'PATCH'
  });
}
```

---

## 🎯 Key Features

✅ **4-Level Alert System** (INFO → WARNING → DANGER → CRITICAL)  
✅ **Automatic Calculation** (spent amount vs budget limit)  
✅ **Read/Unread Tracking** (with timestamps)  
✅ **Filtering Options** (by level, budget, read status)  
✅ **Detailed Messages** (context-aware alerts)  
✅ **RESTful API** (9 endpoints)  
✅ **Comprehensive Logging** (all operations tracked)  
✅ **Database Persistence** (alerts stored in DB)  

---

## 📂 Files Created

1. `model/BudgetAlert.java` - Entity (170 lines)
2. `repository/BudgetAlertRepository.java` - Data access (40 lines)
3. `dto/BudgetAlertDTO.java` - DTO (140 lines)
4. `service/BudgetAlertService.java` - Business logic (280 lines)
5. `controller/BudgetAlertController.java` - API endpoints (200 lines)
6. `database/schema.sql` - Updated with budget_alerts table

**Total**: ~830 lines of production-ready code

---

## 🔧 Configuration

No configuration needed! The system works out of the box with:
- Default thresholds (50%, 75%, 90%, 100%)
- Automatic calculations
- SLF4J logging
- JWT authentication

---

## 📖 Full Documentation

See `BUDGET_ALERT_SYSTEM.md` for complete documentation including:
- Architecture details
- Alert generation logic
- Security considerations
- Performance optimization
- Frontend integration examples
- Testing strategies
- Maintenance procedures
