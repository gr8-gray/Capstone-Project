# Smart Expense Tracker - AI Coding Agent Instructions

## Project Overview
Full-stack expense tracking application with JWT authentication. **Spring Boot 3.2.0** backend + **vanilla JavaScript** frontend + **MySQL 8.0** in Docker.

Package structure: `com.yourapp.expensetracker.expense_api.*`

## Critical Architecture Patterns

### 3-Layer Backend Architecture
```
Controller → Service → Repository (JPA)
```
- **Controllers** (`controller/`): REST endpoints, input validation with `@Valid`, SLF4J logging
- **Services** (`service/`): Business logic, transaction management, alert generation
- **Repositories** (`repository/`): JPA interfaces extending `JpaRepository<T, Long>`

**Key Controllers (6 total):**
- `AuthController` → `/api/auth` (login, register, logout)
- `ExpenseController` → `/api/expenses` (full CRUD + search/filter)
- `BudgetController` → `/api/budgets` (budget management)
- `BudgetAlertController` → `/api/budget-alerts` (4-level alert system)
- `CategoryController` → `/api/categories` (category CRUD)
- `ReportController` → `/api/reports` (analytics/summaries)

### JWT Authentication Flow
1. Login/Register → `AuthService.authenticate()` → returns `JwtAuthResponse` with 24h token
2. Frontend stores token in `localStorage` → all requests use `authenticatedFetch()` in `frontend/auth.js`
3. `JwtAuthenticationFilter` intercepts requests → validates token → sets `SecurityContext`
4. Protected endpoints require `@PreAuthorize("hasRole('USER')")` (not yet fully implemented)

**JWT Configuration:**
- Secret: `app.jwt.secret` (256+ bits, HS256)
- Expiration: `app.jwt.expiration` (default 86400000ms = 24h)
- Provider: `JwtTokenProvider` in `security/`

### Frontend Authentication Pattern
ALL API calls MUST use `authenticatedFetch()` from `auth.js`:
```javascript
const response = await authenticatedFetch(`${API_BASE_URL}/expenses`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(data)
});
```
**Never** use raw `fetch()` for authenticated endpoints - it won't include the Bearer token.

## Security Conventions

### SQL Injection Prevention (Multi-Layer)
1. **JPA Parameterized Queries**: All repositories use `@Query` with `@Param` - NEVER `nativeQuery=true` with string concatenation
2. **Custom Validation**: `@NoSqlInjection` annotation on all string inputs (applies `SqlInjectionValidator`)
3. **Pattern Detection**: See `validation/SqlInjectionValidator.java` for blocked patterns (SQL keywords, comments, OR/AND tricks)

Example from `ExpenseRepository`:
```java
@Query("SELECT e FROM Expense e WHERE e.category = :category")
List<Expense> findByCategory(@Param("category") String category);
```

### Password Security
- **BCrypt** hashing with strength 12: `BCryptPasswordEncoder(12)` in `SecurityConfig`
- Never log passwords - use `@JsonIgnore` on password fields in entities/DTOs

## Development Workflows

### Local Development Setup (Docker + Maven)
```powershell
# Start MySQL container (ALWAYS do this first)
docker compose up -d
# Wait 15-20s for health check, then:
.\mvnw.cmd spring-boot:run

# Stop everything:
docker compose down
```

**Automated script:** `start-with-docker.ps1` does health checking automatically

**Connection Details:**
- MySQL: `localhost:3306`, DB: `expense_db`, User: `expense_user`, Pass: `expense_password`
- Backend: `localhost:8080`
- Frontend: Open `frontend/dashboard.html` directly (no server needed)

### Testing Strategy
- **Unit tests**: H2 in-memory DB (see `src/test/resources/application-test.properties`)
- **Integration tests**: `@SpringBootTest` with `@Transactional` (auto-rollback)
- **40+ test methods** focusing on: SQL injection validation, CRUD operations, budget alerts, reporting

Run tests:
```powershell
.\mvnw.cmd test
```

### Database Schema Management
- **Primary source of truth:** `database/schema.sql`
- **Hibernate DDL:** `spring.jpa.hibernate.ddl-auto=update` (in `application.properties`)
- **DO NOT** manually edit tables - changes should be in both entity classes AND schema.sql

**Key Tables:**
- `expenses` (7 columns + 7 indexes for performance)
- `categories` (pre-populated with 16 default categories)
- `users` (for JWT auth)
- `budgets` (period tracking: DAILY/WEEKLY/MONTHLY)
- `budget_alerts` (4 severity levels: INFO/WARNING/DANGER/CRITICAL at 50%/75%/90%/100%)

**Index Strategy (UPDATED Nov 30, 2025):**
Composite indexes added for all common query patterns:

**Expenses table:**
- `idx_user_id`, `idx_user_category`, `idx_user_date`, `idx_user_category_date` (user-filtered queries)
- `idx_user_amount`, `idx_user_date_amount` (reporting/analytics)
- `idx_category_date` (legacy non-user queries)

**Budgets table:**
- `idx_user_id`, `idx_user_category`, `idx_user_dates` (budget lookups)
- `idx_category_dates` (date range overlaps)

**Budget_alerts table:**
- `idx_budget_is_read`, `idx_is_read_created` (unread alert queries)
- `idx_is_read_alert_level` (critical alerts covering index)
- `idx_is_read_read_at` (cleanup queries)

**Migration:** Run `database/migrations/001_add_missing_indexes.sql` on existing databases. New databases use updated `schema.sql`.

## Budget Alert System (Critical Feature)

### Alert Generation Logic
Located in `BudgetAlertService.checkAndGenerateAlerts()`:
1. Calculate spent amount: SUM expenses in budget period
2. Calculate percentage: `(spent / budgetAmount) * 100`
3. Generate alert at thresholds:
   - 50% → INFO
   - 75% → WARNING
   - 90% → DANGER
   - 100% → CRITICAL

**Alert deduplication:** Only creates new alert if none exists at same level for same budget

### Triggering Alerts
- Automatically called after expense create/update/delete
- Manual trigger: `POST /api/budget-alerts/check-budgets`

## Error Handling Standards

### Global Exception Handler
`GlobalExceptionHandler` provides consistent error responses:
```json
{
  "timestamp": "2025-11-30T...",
  "status": 400,
  "error": "Validation Failed",
  "message": "Description cannot be empty",
  "path": "/api/expenses"
}
```

**Handle these specific exceptions:**
- `BadCredentialsException` → 401 "Invalid username or password"
- `ResourceNotFoundException` → 404 with resource details
- `MethodArgumentNotValidException` → 400 with field-level errors
- `RuntimeException` → 500 (logged with stack trace)

### Logging Standards (SLF4J)
Use appropriate levels in all services/controllers:
```java
logger.info("User {} logged in successfully", username);
logger.warn("Failed login attempt for user: {}", username);
logger.error("Unexpected error processing expense", exception);
```

**Log files:** `logs/application.log` (rotating, max 10MB, 30 days retention)

## Query Performance Patterns

### Composite Index Usage Rules
When writing repository queries, follow the **leftmost prefix rule**:

**✅ Good (uses indexes):**
```java
// Uses idx_user_category_date
findByUserIdAndCategoryAndDateBetween(userId, category, startDate, endDate)

// Uses idx_user_category
findByUserIdAndCategory(userId, category)

// Uses idx_user_id
findByUserId(userId)
```

**❌ Bad (misses indexes):**
```java
// idx_user_category NOT used - missing user_id prefix
findByCategoryAndUserId(category, userId)

// idx_user_date NOT used optimally - different order
findByDateAndUserId(date, userId)
```

**Index Coverage:** Composite indexes like `idx_user_category_date (user_id, category, date)` can satisfy queries for:
- `user_id` alone
- `user_id + category`
- `user_id + category + date`

But NOT for `category` alone or `date` alone.

### N+1 Query Prevention
ALL user-joined queries have `WithUser` variants using `JOIN FETCH`:
```java
// ❌ N+1 problem: findByUserId() triggers lazy load per expense
List<Expense> expenses = expenseRepository.findByUserId(userId);

// ✅ Single query: JOIN FETCH loads user eagerly
List<Expense> expenses = expenseRepository.findByUserIdWithUser(userId);
```

Use `WithUser` methods for list views, paginated methods for large datasets.

## Common Pitfalls

1. **Docker not running**: Backend fails with MySQL connection errors → check `docker ps`
2. **Frontend CORS errors**: Backend must be on `localhost:8080` (configured in `SecurityConfig.corsConfigurationSource()`)
3. **Token expiration**: 24h JWT expires → frontend shows "Unauthorized" → user must re-login
4. **Missing @NoSqlInjection**: All new string fields in entities/DTOs need this annotation
5. **Service layer bypass**: Controllers should NEVER call repositories directly → always go through services
6. **Maven wrapper**: Use `.\mvnw.cmd` on Windows, `./mvnw` on Unix
7. **Missing composite indexes**: When adding new query methods, check if composite indexes exist for the filter combination - follow leftmost prefix rule

## Key Files Reference

- `pom.xml` - Java 17, Spring Boot 3.2.0, JWT 0.12.3, MySQL Connector
- `application.properties` - DB config, JWT settings, logging levels (⚠️ contains production credentials)
- `docker-compose.yml` - MySQL 8.0 service with health checks
- `database/schema.sql` - Complete DB schema with indexes and constraints
- `database/INDEX_STRATEGY.md` - **Comprehensive index usage guide, query optimization patterns, leftmost prefix rule**
- `database/migrations/001_add_missing_indexes.sql` - Migration script for existing databases
- `frontend/auth.js` - Authentication module (LOGIN CRITICAL: all API calls)
- `security/JwtTokenProvider.java` - Token generation/validation
- `validation/SqlInjectionValidator.java` - Security validation patterns

## Documentation Hierarchy
For deep dives, read in this order:
1. `QUICKSTART.md` - 5-minute setup guide
2. `ARCHITECTURE.md` - Data flow diagrams
3. `AUTHENTICATION.md` - JWT implementation details
4. `BUDGET_ALERT_SYSTEM.md` - Alert generation logic
5. `SQL_INJECTION_PREVENTION.md` - Security measures
6. `ERROR_HANDLING_LOGGING.md` - Exception handling patterns
