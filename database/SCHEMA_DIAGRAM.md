# Database Schema UML Diagram
## Smart Expense Tracker - Entity Relationship Diagram

**Database Engineer:** Michael Basye  
**UMGC CMSC 495 Capstone Project - Group 3**

---

## Entity Relationship Diagram

```mermaid
erDiagram
    expenses ||--o{ categories : "belongs to"
    expenses }o--|| users : "owned by (Phase 2)"
    budgets }o--|| users : "owned by (Phase 2)"
    budgets ||--o{ categories : "for category"

    expenses {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR description "NOT NULL, max 255"
        DECIMAL amount "NOT NULL, (12,2), CHECK > 0"
        VARCHAR category "NOT NULL, max 100"
        DATE date "NOT NULL, CHECK not future"
        DATETIME created_at "NOT NULL, AUTO"
        DATETIME updated_at "AUTO"
        BIGINT user_id FK "NULL (Phase 2)"
    }

    categories {
        INT id PK "AUTO_INCREMENT"
        VARCHAR name UK "NOT NULL, max 100, UNIQUE"
        TEXT description "NULL"
        DATETIME created_at "NOT NULL, AUTO"
    }

    users {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR username UK "NOT NULL, max 50, UNIQUE"
        VARCHAR email UK "NOT NULL, max 100, UNIQUE"
        VARCHAR password_hash "NOT NULL, max 255"
        VARCHAR first_name "NULL, max 50"
        VARCHAR last_name "NULL, max 50"
        DATETIME created_at "NOT NULL, AUTO"
        DATETIME updated_at "AUTO"
        DATETIME last_login "NULL"
        BOOLEAN is_active "DEFAULT TRUE"
    }

    budgets {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR category "NOT NULL, max 100"
        DECIMAL amount "NOT NULL, (12,2), CHECK > 0"
        INT month "NOT NULL, CHECK 1-12"
        INT year "NOT NULL, CHECK >= 2020"
        DATETIME created_at "NOT NULL, AUTO"
        DATETIME updated_at "AUTO"
        BIGINT user_id FK "NULL (Phase 2)"
    }
```

---

## Database Views

```mermaid
graph TD
    expenses[expenses table]
    
    expenses --> v_monthly[v_monthly_expenses]
    expenses --> v_category[v_category_totals]
    expenses --> v_recent[v_recent_expenses]
    
    v_monthly[v_monthly_expenses<br/>Aggregates by year/month/category]
    v_category[v_category_totals<br/>Sums and counts by category]
    v_recent[v_recent_expenses<br/>Last 30 days filter]
    
    style v_monthly fill:#e1f5ff
    style v_category fill:#e1f5ff
    style v_recent fill:#e1f5ff
```

---

## Stored Procedures

```mermaid
graph LR
    expenses[(expenses table)]
    budgets[(budgets table)]
    
    expenses --> sp1[sp_get_monthly_summary]
    expenses --> sp2[sp_get_expenses_by_date_range]
    budgets --> sp3[sp_budget_status]
    expenses --> sp3
    
    sp1 --> result1[Monthly breakdown<br/>by category]
    sp2 --> result2[Filtered expenses<br/>by date range]
    sp3 --> result3[Budget vs actual<br/>comparison]
    
    style sp1 fill:#ffe1e1
    style sp2 fill:#ffe1e1
    style sp3 fill:#ffe1e1
```

---

## Index Structure

### expenses Table Indexes

| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| PRIMARY | id | Clustered | Primary key access |
| idx_category | category | Non-clustered | Category filtering |
| idx_date | date | Non-clustered | Date range queries |
| idx_created_at | created_at | Non-clustered | Audit queries |
| idx_updated_at | updated_at | Non-clustered | Recent changes |
| idx_category_date | category, date | Composite | Combined filters |
| idx_amount | amount | Non-clustered | Amount-based queries |
| idx_description | description(100) | Partial | Text search |

### Categories Table Indexes

| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| PRIMARY | id | Clustered | Primary key access |
| UNIQUE | name | Unique | Enforce unique names |

### Users Table Indexes

| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| PRIMARY | id | Clustered | Primary key access |
| UNIQUE | username | Unique | Login lookup |
| UNIQUE | email | Unique | Email uniqueness |
| idx_username | username | Non-clustered | Fast username lookup |
| idx_email | email | Non-clustered | Fast email lookup |

### Budgets Table Indexes

| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| PRIMARY | id | Clustered | Primary key access |
| UNIQUE | category, month, year | Composite Unique | No duplicate budgets |
| idx_month_year | month, year | Composite | Period queries |

---

## Data Flow Diagram

```mermaid
flowchart TD
    app[Spring Boot Application]
    
    app -->|CRUD Operations| expenses[expenses table]
    app -->|Reference Data| categories[categories table]
    app -->|Future: Auth| users[users table]
    app -->|Future: Budget| budgets[budgets table]
    
    expenses -->|Aggregation| views[Database Views]
    expenses -->|Analysis| procs[Stored Procedures]
    budgets -->|Comparison| procs
    
    views -->|Pre-computed| analytics[Analytics API]
    procs -->|Complex Queries| reports[Reports API]
    
    categories -->|Validation| app
    
    style app fill:#90EE90
    style expenses fill:#FFD700
    style categories fill:#FFD700
    style users fill:#D3D3D3
    style budgets fill:#D3D3D3
    style views fill:#87CEEB
    style procs fill:#FFA07A
```

---

## Schema Evolution (Phase 2)

### Current State (Phase 1)
```mermaid
graph TD
    expenses[expenses<br/>✅ Active]
    categories[categories<br/>✅ Active]
    users[users<br/>📝 Prepared]
    budgets[budgets<br/>📝 Prepared]
    
    expenses -.->|Future FK| users
    budgets -.->|Future FK| users
    
    style expenses fill:#90EE90
    style categories fill:#90EE90
    style users fill:#FFE4B5
    style budgets fill:#FFE4B5
```

### Future State (Phase 2)
```mermaid
graph TD
    expenses[expenses<br/>+ user_id FK]
    categories[categories<br/>No changes]
    users[users<br/>✅ Activated]
    budgets[budgets<br/>+ user_id FK]
    
    expenses -->|FOREIGN KEY| users
    budgets -->|FOREIGN KEY| users
    expenses -->|category| categories
    budgets -->|category| categories
    
    style expenses fill:#90EE90
    style categories fill:#90EE90
    style users fill:#90EE90
    style budgets fill:#90EE90
```

---

## Constraints and Triggers

### Check Constraints

```mermaid
graph TD
    expenses[expenses table]
    budgets[budgets table]
    
    expenses --> chk1[chk_amount_positive<br/>amount > 0]
    expenses --> chk2[chk_description_not_empty<br/>description length > 0]
    
    budgets --> chk3[chk_budget_positive<br/>amount > 0]
    budgets --> chk4[chk_month_valid<br/>month BETWEEN 1 AND 12]
    budgets --> chk5[chk_year_valid<br/>year >= 2020]
    
    style chk1 fill:#FFB6C1
    style chk2 fill:#FFB6C1
    style chk3 fill:#FFB6C1
    style chk4 fill:#FFB6C1
    style chk5 fill:#FFB6C1
```

### Triggers

```mermaid
graph LR
    insert[INSERT Operation]
    
    insert --> trigger[trg_before_insert_expense]
    
    trigger --> validate1{amount > 0?}
    trigger --> validate2{date <= today?}
    
    validate1 -->|NO| error1[SIGNAL ERROR:<br/>Amount must be positive]
    validate2 -->|NO| error2[SIGNAL ERROR:<br/>Date cannot be future]
    
    validate1 -->|YES| success[Insert Allowed]
    validate2 -->|YES| success
    
    style trigger fill:#98FB98
    style error1 fill:#FF6B6B
    style error2 fill:#FF6B6B
    style success fill:#90EE90
```

---

## Category Hierarchy

```mermaid
graph TD
    root[16 Predefined Categories]
    
    root --> spending[Spending Categories]
    root --> income[Revenue Categories - Future]
    
    spending --> daily[Daily Expenses]
    spending --> recurring[Recurring Bills]
    spending --> discretionary[Discretionary]
    spending --> investment[Financial]
    
    daily --> food[Food & Dining]
    daily --> transport[Transportation]
    daily --> shopping[Shopping]
    
    recurring --> bills[Bills & Utilities]
    recurring --> insurance[Insurance]
    recurring --> subs[Subscriptions]
    
    discretionary --> entertainment[Entertainment]
    discretionary --> travel[Travel]
    discretionary --> gifts[Gifts & Donations]
    discretionary --> personal[Personal Care]
    
    investment --> investments[Investments]
    investment --> taxes[Taxes]
    
    spending --> other1[Other Categories]
    other1 --> healthcare[Healthcare]
    other1 --> education[Education]
    other1 --> home[Home & Garden]
    other1 --> business[Business]
    other1 --> other[Other]
    
    style root fill:#FFD700
    style spending fill:#90EE90
    style daily fill:#87CEEB
    style recurring fill:#FFA07A
    style discretionary fill:#DDA0DD
    style investment fill:#F0E68C
```

---

## Query Performance Flow

```mermaid
graph TD
    query[SQL Query]
    
    query --> analyze{Query Type?}
    
    analyze -->|Simple SELECT| idx1[Use Index Scan]
    analyze -->|Date Range| idx2[idx_date]
    analyze -->|Category Filter| idx3[idx_category]
    analyze -->|Combined Filter| idx4[idx_category_date]
    analyze -->|Aggregation| idx5[View or Index Scan]
    
    idx1 --> cache[Query Cache]
    idx2 --> cache
    idx3 --> cache
    idx4 --> cache
    idx5 --> cache
    
    cache --> result[Fast Results]
    
    style query fill:#FFD700
    style idx1 fill:#90EE90
    style idx2 fill:#90EE90
    style idx3 fill:#90EE90
    style idx4 fill:#90EE90
    style idx5 fill:#90EE90
    style result fill:#87CEEB
```

---

## Summary Statistics

| Component | Count | Status |
|-----------|-------|--------|
| **Tables** | 4 | ✅ Complete |
| **Indexes** | 17 total | ✅ Optimized |
| **Views** | 3 | ✅ Active |
| **Stored Procedures** | 3 | ✅ Active |
| **Triggers** | 1 | ✅ Active |
| **Check Constraints** | 7 | ✅ Enforced |
| **Foreign Keys** | 0 (2 prepared) | 📝 Phase 2 |
| **Categories** | 16 | ✅ Seeded |

---

**Diagram Created By:** Michael Basye - Database Engineer  
**Date:** November 3, 2025  
**Schema Version:** 1.0

---

## How to View These Diagrams

### On GitHub
These Mermaid diagrams render automatically when viewing this file on GitHub.

### Locally
1. **VS Code:** Install "Markdown Preview Mermaid Support" extension
2. **IntelliJ IDEA:** Mermaid plugin available
3. **Online:** Copy diagram code to https://mermaid.live/

### Export Options
- **PNG/SVG:** Use mermaid.live to export images
- **PDF:** Print GitHub page to PDF
- **Documentation:** Embed in project documentation
