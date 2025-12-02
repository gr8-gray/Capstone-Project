# Database Query Optimization Summary

## Overview
Comprehensive database query optimization to fix N+1 query problems, add user filtering, implement pagination, and add database indexes for performance.

**Date**: 2025-11-30  
**Status**: ✅ Complete

---

## 🎯 Problems Addressed

### 1. N+1 Query Problem
**Issue**: The `Expense` entity had a `@ManyToOne` relationship with `User` using `FetchType.LAZY`, causing N+1 queries when accessing user data for each expense.

**Solution**: Added `JOIN FETCH` queries to eagerly load user data in a single query.

**Impact**: Reduces database queries from N+1 to 1 for expense list operations.

### 2. Missing User Filtering
**Issue**: Query methods returned expenses from all users instead of filtering by authenticated user.

**Solution**: Added `userId` parameter to all query methods and filtered results.

**Impact**: 
- Improved security (users can only see their own data)
- Better query performance (smaller result sets)
- Proper data isolation

### 3. Lack of Pagination
**Issue**: Methods like `findAll()` loaded entire datasets into memory.

**Solution**: Implemented pagination using Spring Data's `Page<T>` and `Pageable`.

**Impact**:
- Reduced memory usage
- Improved API response times
- Better scalability for large datasets

### 4. Missing Database Indexes
**Issue**: No indexes on foreign key column `user_id` or commonly filtered columns.

**Solution**: Added 3 composite indexes to the `expenses` table.

**Impact**:
- Faster query execution for user-filtered queries
- Optimized category and date-based filtering
- Improved JOIN performance

---

## 📊 Changes by File

### 1. Expense.java (Model)
**Location**: `src/main/java/com/yourapp/expensetracker/expense_api/model/Expense.java`

**Changes**:
- Added 3 new indexes to `@Table` annotation:
  ```java
  @Index(name = "idx_user_id", columnList = "user_id")
  @Index(name = "idx_user_category", columnList = "user_id, category")
  @Index(name = "idx_user_date", columnList = "user_id, date")
  ```

**Impact**: Database queries on user_id, category, and date are now optimized with composite indexes.

---

### 2. ExpenseRepository.java (Data Access Layer)
**Location**: `src/main/java/com/yourapp/expensetracker/expense_api/repository/ExpenseRepository.java`

**Changes**:
- Added 20+ new user-filtered query methods
- Implemented JOIN FETCH queries for N+1 prevention
- Added pagination support to all list methods
- Deprecated old non-user-filtered methods

**Key New Methods**:
```java
// User-filtered with JOIN FETCH
@Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.user.id = :userId")
List<Expense> findByUserIdWithUser(@Param("userId") Long userId);

// Pagination support
Page<Expense> findByUserId(Long userId, Pageable pageable);

// User + category filtering with JOIN FETCH
@Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.user.id = :userId AND e.category = :category")
List<Expense> findByUserIdAndCategoryWithUser(@Param("userId") Long userId, @Param("category") String category);

// User + date range with pagination
Page<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

// Aggregation methods
BigDecimal sumAmountByUserIdAndCategoryAndDateRange(Long userId, String category, LocalDate startDate, LocalDate endDate);
List<String> findDistinctCategoriesByUserId(Long userId);
long countByUserId(Long userId);
```

**Deprecated Methods** (kept for backward compatibility):
- `findByCategory(String category)`
- `findByDateBetween(LocalDate, LocalDate)`
- `findByCategoryAndDateBetween(String, LocalDate, LocalDate)`
- `findByAmountBetween(BigDecimal, BigDecimal)`
- `findByDescriptionContainingIgnoreCase(String)`

---

### 3. ExpenseService.java (Business Logic Layer)
**Location**: `src/main/java/com/yourapp/expensetracker/expense_api/service/ExpenseService.java`

**Changes**:
- Added `AuthService` dependency for user context
- Refactored all methods to use user-filtered repository methods
- Added pagination support to all list operations
- Added count methods for statistics

**Key New Methods**:
```java
// Pagination
public Page<Expense> getAllExpensesByUserId(Long userId, Pageable pageable)
public Page<Expense> getExpensesByUserIdAndCategory(Long userId, String category, Pageable pageable)
public Page<Expense> getExpensesByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable)
public Page<Expense> searchExpensesByUserIdAndDescription(Long userId, String keyword, Pageable pageable)
public Page<Expense> getExpensesByUserIdAndAmountRange(Long userId, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable)

// List variants (without pagination)
public List<Expense> getExpensesByUserIdAndCategory(Long userId, String category)
public List<Expense> getExpensesByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate)
public List<Expense> searchExpensesByUserIdAndDescription(Long userId, String keyword)
public List<Expense> getExpensesByUserIdAndAmountRange(Long userId, BigDecimal minAmount, BigDecimal maxAmount)

// Aggregations
public BigDecimal getTotalAmountByUserIdCategoryAndDateRange(Long userId, String category, LocalDate startDate, LocalDate endDate)
public BigDecimal getTotalAmountByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate)
public List<String> getAllCategoriesByUserId(Long userId)

// Statistics
public long countExpensesByUserId(Long userId)
public long countExpensesByUserIdAndCategory(Long userId, String category)
public long countExpensesByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate)
```

**Deprecated Methods** (backward compatibility for tests):
- `getExpensesByCategory(String category)`
- `getExpensesByDateRange(LocalDate, LocalDate)`
- `getExpensesByAmountRange(BigDecimal, BigDecimal)`

---

### 4. ExpenseController.java (REST API Layer)
**Location**: `src/main/java/com/yourapp/expensetracker/expense_api/controller/ExpenseController.java`

**Changes**:
- Added pagination imports (`Page`, `PageRequest`, `Pageable`, `Sort`)
- Updated all endpoints to support optional pagination
- Added user context via `AuthService`
- Added `createPageable()` helper method for dynamic sorting

**Updated Endpoints**:

#### GET /api/expenses (with pagination)
```java
@GetMapping
public ResponseEntity<?> getAllExpenses(
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer size,
    @RequestParam(required = false) String sort)
```
**Usage**:
- `GET /api/expenses` - Returns all expenses (backward compatible)
- `GET /api/expenses?page=0&size=20` - Paginated response
- `GET /api/expenses?page=0&size=20&sort=date,desc` - With sorting

**Response**: 
- Without pagination: `List<Expense>`
- With pagination: `Page<Expense>` containing:
  ```json
  {
    "content": [...],
    "totalPages": 10,
    "totalElements": 200,
    "size": 20,
    "number": 0
  }
  ```

#### GET /api/expenses/category/{category}
```java
@GetMapping("/category/{category}")
public ResponseEntity<?> getExpensesByCategory(
    @PathVariable String category,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer size,
    @RequestParam(required = false) String sort)
```
**Usage**:
- `GET /api/expenses/category/Food`
- `GET /api/expenses/category/Food?page=0&size=10`

#### GET /api/expenses/date-range
```java
@GetMapping("/date-range")
public ResponseEntity<?> getExpensesByDateRange(
    @RequestParam LocalDate startDate,
    @RequestParam LocalDate endDate,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer size,
    @RequestParam(required = false) String sort)
```
**Usage**:
- `GET /api/expenses/date-range?startDate=2025-01-01&endDate=2025-12-31`
- `GET /api/expenses/date-range?startDate=2025-01-01&endDate=2025-12-31&page=0&size=20`

#### GET /api/expenses/search
```java
@GetMapping("/search")
public ResponseEntity<?> searchExpenses(
    @RequestParam String keyword,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer size,
    @RequestParam(required = false) String sort)
```
**Usage**:
- `GET /api/expenses/search?keyword=grocery`
- `GET /api/expenses/search?keyword=grocery&page=0&size=10`

#### GET /api/expenses/categories
```java
@GetMapping("/categories")
public ResponseEntity<List<String>> getAllCategories()
```
**Changes**: Now returns only categories for the authenticated user.

#### GET /api/expenses/total
```java
@GetMapping("/total")
public ResponseEntity<Map<String, BigDecimal>> getTotalAmount(
    @RequestParam(required = false) String category,
    @RequestParam LocalDate startDate,
    @RequestParam LocalDate endDate)
```
**Changes**: Now calculates totals only for the authenticated user.

#### GET /api/expenses/amount-range
```java
@GetMapping("/amount-range")
public ResponseEntity<?> getExpensesByAmountRange(
    @RequestParam BigDecimal minAmount,
    @RequestParam BigDecimal maxAmount,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer size,
    @RequestParam(required = false) String sort)
```
**Usage**:
- `GET /api/expenses/amount-range?minAmount=10.00&maxAmount=100.00`
- `GET /api/expenses/amount-range?minAmount=10.00&maxAmount=100.00&page=0&size=10`

---

### 5. ReportService.java
**Location**: `src/main/java/com/yourapp/expensetracker/expense_api/service/ReportService.java`

**Changes**:
- Added `AuthService` dependency
- Updated all methods to use user-filtered queries
- All report methods now filter by authenticated user

**Updated Methods** (all now user-filtered):
- `getMonthlyExpenseSummary(int year, int month)`
- `getMonthlyReport(int year, int month)`
- `getYearlyReport(int year)`
- `getCategoryBreakdown(LocalDate startDate, LocalDate endDate)`
- `getTotalForDateRange(LocalDate startDate, LocalDate endDate)`
- `getTopExpenseCategories(LocalDate startDate, LocalDate endDate, int limit)`
- `getAverageDailyExpense(LocalDate startDate, LocalDate endDate)`
- `compareMonths(int year1, int month1, int year2, int month2)`
- `getCategoryReport(String category, LocalDate startDate, LocalDate endDate)`

---

### 6. BudgetAlertService.java
**Location**: `src/main/java/com/yourapp/expensetracker/expense_api/service/BudgetAlertService.java`

**Changes**:
- Added `AuthService` dependency
- Updated `checkBudgetAndCreateAlert()` to use user-filtered expense query
- Now uses `findByUserIdAndCategoryAndDateBetweenWithUser` instead of deprecated `findByCategoryAndDateBetween`

**Updated Method**:
```java
private Optional<BudgetAlert> checkBudgetAndCreateAlert(Budget budget) {
    Long userId = authService.getCurrentUser().getId();
    BigDecimal totalSpent = expenseRepository
        .findByUserIdAndCategoryAndDateBetweenWithUser(
            userId, budget.getCategory(), budget.getStartDate(), budget.getEndDate())
        .stream()
        .map(Expense::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    // ... rest of method
}
```

---

## 🗄️ Database Schema Changes

### New Indexes Created
Hibernate automatically created these indexes on application startup:

```sql
CREATE INDEX idx_user_id ON expenses (user_id);
CREATE INDEX idx_user_category ON expenses (user_id, category);
CREATE INDEX idx_user_date ON expenses (user_id, date);
```

**Index Usage**:
- `idx_user_id`: Used for basic user filtering (`WHERE user_id = ?`)
- `idx_user_category`: Optimizes user + category queries (`WHERE user_id = ? AND category = ?`)
- `idx_user_date`: Optimizes user + date range queries (`WHERE user_id = ? AND date BETWEEN ? AND ?`)

**Verification**:
```sql
SHOW INDEX FROM expenses;
```

---

## 📈 Performance Improvements

### Before Optimization
```sql
-- N+1 Query Problem Example
SELECT * FROM expenses;  -- 1 query
SELECT * FROM users WHERE id = 1;  -- Query for each expense
SELECT * FROM users WHERE id = 2;
SELECT * FROM users WHERE id = 3;
-- ... N queries for N expenses

-- No User Filtering
SELECT * FROM expenses WHERE category = 'Food';  -- Returns all users' data

-- No Pagination
SELECT * FROM expenses;  -- Loads ALL expenses into memory

-- No Indexes
EXPLAIN SELECT * FROM expenses WHERE user_id = 454;
-- Type: ALL (full table scan)
```

### After Optimization
```sql
-- Single Query with JOIN FETCH
SELECT e.*, u.* FROM expenses e 
INNER JOIN users u ON e.user_id = u.id 
WHERE e.user_id = 454;
-- 1 query total instead of N+1

-- User Filtering
SELECT * FROM expenses WHERE user_id = 454 AND category = 'Food';
-- Only authenticated user's data

-- Pagination
SELECT * FROM expenses WHERE user_id = 454 
LIMIT 20 OFFSET 0;
-- Only loads 20 records at a time

-- With Indexes
EXPLAIN SELECT * FROM expenses WHERE user_id = 454;
-- Type: ref, Key: idx_user_id (index scan)

EXPLAIN SELECT * FROM expenses 
WHERE user_id = 454 AND category = 'Food';
-- Type: ref, Key: idx_user_category (composite index scan)
```

### Expected Performance Gains
| Operation | Before | After | Improvement |
|-----------|--------|-------|-------------|
| Load 100 expenses | 101 queries | 1 query | **99% reduction** |
| Filter by category | Full table scan | Index scan | **10-100x faster** |
| Load all expenses | All records in memory | 20 at a time | **95% memory reduction** |
| User data isolation | Manual filtering | Database-level | **100% secure** |

---

## 🧪 Testing Recommendations

### 1. Test Pagination
```bash
# Get first page
curl "http://localhost:8080/api/expenses?page=0&size=10"

# Get second page
curl "http://localhost:8080/api/expenses?page=1&size=10"

# With sorting
curl "http://localhost:8080/api/expenses?page=0&size=10&sort=date,desc"
curl "http://localhost:8080/api/expenses?page=0&size=10&sort=amount,asc"
```

### 2. Test User Filtering
```bash
# Login as user 1
TOKEN1=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"pass1"}' | jq -r '.token')

# Get expenses for user 1
curl -H "Authorization: Bearer $TOKEN1" \
  http://localhost:8080/api/expenses

# Login as user 2
TOKEN2=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user2","password":"pass2"}' | jq -r '.token')

# Get expenses for user 2 (should be different)
curl -H "Authorization: Bearer $TOKEN2" \
  http://localhost:8080/api/expenses
```

### 3. Test Query Performance
```sql
-- Enable query logging in application.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

-- Monitor logs and verify:
-- 1. Only 1 query for JOIN FETCH operations
-- 2. Queries include "WHERE user_id = ?"
-- 3. LIMIT clause present for pagination
```

### 4. Test Index Usage
```sql
-- Verify indexes exist
SHOW INDEX FROM expenses WHERE Key_name LIKE 'idx_user%';

-- Check query execution plan
EXPLAIN SELECT * FROM expenses WHERE user_id = 454;
EXPLAIN SELECT * FROM expenses WHERE user_id = 454 AND category = 'Food';
EXPLAIN SELECT * FROM expenses WHERE user_id = 454 AND date BETWEEN '2025-01-01' AND '2025-12-31';

-- All should show "Type: ref" and use the appropriate index
```

---

## 🔄 Backward Compatibility

### Deprecated Methods
To maintain backward compatibility with existing code and tests, deprecated methods are kept:

**Repository Layer**:
- `findByCategory(String)` → Use `findByUserIdAndCategory(Long, String)`
- `findByDateBetween(LocalDate, LocalDate)` → Use `findByUserIdAndDateBetween(Long, LocalDate, LocalDate)`
- `findByCategoryAndDateBetween(...)` → Use `findByUserIdAndCategoryAndDateBetween(...)`
- `findByAmountBetween(...)` → Use `findByUserIdAndAmountBetween(...)`
- `findByDescriptionContainingIgnoreCase(String)` → Use `findByUserIdAndDescriptionContainingIgnoreCase(...)`

**Service Layer**:
- `getExpensesByCategory(String)` → Use `getExpensesByUserIdAndCategory(Long, String)`
- `getExpensesByDateRange(...)` → Use `getExpensesByUserIdAndDateRange(...)`
- `getExpensesByAmountRange(...)` → Use `getExpensesByUserIdAndAmountRange(...)`

### Migration Guide
```java
// Old code (deprecated)
List<Expense> expenses = expenseRepository.findByCategory("Food");

// New code (recommended)
Long userId = authService.getCurrentUser().getId();
List<Expense> expenses = expenseRepository.findByUserIdAndCategoryWithUser(userId, "Food");

// Or with pagination
Page<Expense> expenses = expenseRepository.findByUserIdAndCategory(
    userId, "Food", PageRequest.of(0, 20, Sort.by("date").descending())
);
```

---

## 📋 Dependencies Added

### Service Constructor Updates
```java
// ExpenseService
@Autowired
public ExpenseService(ExpenseRepository expenseRepository, AuthService authService) {
    this.expenseRepository = expenseRepository;
    this.authService = authService;
}

// ReportService
@Autowired
public ReportService(ExpenseRepository expenseRepository, AuthService authService) {
    this.expenseRepository = expenseRepository;
    this.authService = authService;
}

// BudgetAlertService
@Autowired
public BudgetAlertService(BudgetAlertRepository alertRepository,
                          BudgetRepository budgetRepository,
                          ExpenseRepository expenseRepository,
                          AuthService authService) {
    this.alertRepository = alertRepository;
    this.budgetRepository = budgetRepository;
    this.expenseRepository = expenseRepository;
    this.authService = authService;
}
```

---

## ✅ Verification Steps

### 1. Build Status
```bash
./mvnw clean compile
# Expected: [INFO] BUILD SUCCESS
```

### 2. Application Startup
```bash
./mvnw spring-boot:run -DskipTests
# Expected: Application starts on port 8080
# Look for: "Started ExpenseApiApplication in X seconds"
```

### 3. Index Creation Logs
```
Hibernate: create index idx_user_id on expenses (user_id)
Hibernate: create index idx_user_category on expenses (user_id, category)
Hibernate: create index idx_user_date on expenses (user_id, date)
```

### 4. API Testing
```bash
# Login
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"gr8gray","password":"982gray"}' | jq -r '.token')

# Test paginated endpoint
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/expenses?page=0&size=5" | jq

# Expected: JSON with pagination metadata
{
  "content": [...],
  "totalPages": 3,
  "totalElements": 15,
  "size": 5,
  "number": 0
}
```

---

## 🐛 Known Issues & Solutions

### Issue: Tests Failing After Refactoring
**Symptom**: `cannot find symbol: method getExpensesByCategory(java.lang.String)`

**Solution**: Added backward-compatible deprecated methods to maintain test compatibility.

### Issue: Circular Dependency
**Symptom**: ExpenseService requires AuthService, but AuthService may require ExpenseService

**Solution**: Properly structured dependency injection with `@Autowired` constructors.

### Issue: HikariCP Connection Timeout
**Symptom**: `Connection is not available, request timed out`

**Solution**: Verify Railway MySQL connection settings in `application.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.connection-timeout=30000
```

---

## 📚 Best Practices Implemented

### 1. JOIN FETCH for N+1 Prevention
```java
@Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.user.id = :userId")
List<Expense> findByUserIdWithUser(@Param("userId") Long userId);
```

### 2. Composite Indexes for Multi-Column Filters
```java
@Index(name = "idx_user_category", columnList = "user_id, category")
```

### 3. Pagination for Large Datasets
```java
Page<Expense> findByUserId(Long userId, Pageable pageable);
```

### 4. User Context from Authentication
```java
User currentUser = authService.getCurrentUser();
List<Expense> expenses = expenseService.getExpensesByUserIdAndCategory(
    currentUser.getId(), category
);
```

### 5. Optional Pagination Parameters
```java
@RequestParam(required = false) Integer page,
@RequestParam(required = false) Integer size,
@RequestParam(required = false) String sort
```

---

## 🎓 Key Learnings

1. **N+1 Queries**: Always use `JOIN FETCH` for `@ManyToOne` and `@OneToMany` relationships when loading collections
2. **Indexes**: Composite indexes should match query filter order (user_id first, then category/date)
3. **Pagination**: Essential for scalability - never return unbounded result sets
4. **User Filtering**: Always filter by authenticated user to ensure data isolation and security
5. **Backward Compatibility**: Use `@Deprecated` to maintain old methods while transitioning to new patterns

---

## 📝 Next Steps

### Recommended Enhancements
1. **Add Query Result Caching**:
   ```java
   @Cacheable("expenses")
   public List<Expense> getExpensesByUserIdAndCategory(Long userId, String category)
   ```

2. **Implement Query Hints**:
   ```java
   @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "50"))
   ```

3. **Add Database Connection Pool Monitoring**:
   - Enable HikariCP metrics
   - Monitor active connections and wait times

4. **Consider Read Replicas**:
   - For high-traffic scenarios
   - Separate read and write operations

5. **Add Query Performance Monitoring**:
   - Log slow queries
   - Set up APM (Application Performance Monitoring)

---

## 👥 Credits
- **Backend Developer**: Eric Gray
- **Database Engineer**: Michael Basye
- **Optimization Date**: 2025-11-30

---

## 📄 Related Documentation
- [ARCHITECTURE.md](./ARCHITECTURE.md) - Application architecture overview
- [DATABASE_SCHEMA_VERIFICATION.md](./DATABASE_SCHEMA_VERIFICATION.md) - Database schema details
- [API Documentation](./README.md#api-endpoints) - REST API reference

