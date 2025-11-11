# Smart Expense Tracker - Integration Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER INTERFACE                          │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  Frontend (HTML/CSS/JavaScript)                           │ │
│  │  - dashboard.html (Main UI)                               │ │
│  │  - script.js (API Integration)                            │ │
│  │  - style.css (Styling)                                    │ │
│  │                                                            │ │
│  │  Features:                                                 │ │
│  │  ✓ Add Expense Form                                       │ │
│  │  ✓ Expense List Table                                     │ │
│  │  ✓ Edit/Delete Actions                                    │ │
│  │  ✓ Total Calculation                                      │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                            │
                            │ HTTP Requests (AJAX)
                            │ http://localhost:8080/api/expenses
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    APPLICATION SERVER                            │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  Spring Boot Backend (Port 8080)                          │ │
│  │                                                            │ │
│  │  REST API Endpoints:                                       │ │
│  │  ┌─────────────────────────────────────────────────────┐  │ │
│  │  │ ExpenseController                                    │  │ │
│  │  │                                                      │  │ │
│  │  │  GET    /api/expenses        → Get all expenses     │  │ │
│  │  │  GET    /api/expenses/{id}   → Get single expense   │  │ │
│  │  │  POST   /api/expenses        → Create expense       │  │ │
│  │  │  PUT    /api/expenses/{id}   → Update expense       │  │ │
│  │  │  DELETE /api/expenses/{id}   → Delete expense       │  │ │
│  │  └─────────────────────────────────────────────────────┘  │ │
│  │                           │                                 │ │
│  │                           ▼                                 │ │
│  │  ┌─────────────────────────────────────────────────────┐  │ │
│  │  │ ExpenseService (Business Logic)                     │  │ │
│  │  │  - Validation                                       │  │ │
│  │  │  - Data Processing                                  │  │ │
│  │  │  - Error Handling                                   │  │ │
│  │  └─────────────────────────────────────────────────────┘  │ │
│  │                           │                                 │ │
│  │                           ▼                                 │ │
│  │  ┌─────────────────────────────────────────────────────┐  │ │
│  │  │ ExpenseRepository (JPA/Hibernate)                   │  │ │
│  │  │  - CRUD Operations                                  │  │ │
│  │  │  - Query Methods                                    │  │ │
│  │  │  - Transaction Management                           │  │ │
│  │  └─────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                            │
                            │ JDBC Connection
                            │ jdbc:mysql://localhost:3306/expense_db
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE LAYER                              │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  MySQL Database (Port 3306)                               │ │
│  │                                                            │ │
│  │  Database: expense_db                                      │ │
│  │  User: expense_user                                        │ │
│  │                                                            │ │
│  │  ┌─────────────────────────────────────────────────────┐  │ │
│  │  │ expenses table                                       │  │ │
│  │  │ ┌─────────────────────────────────────────────────┐ │  │ │
│  │  │ │ id            BIGINT PRIMARY KEY AUTO_INCREMENT │ │  │ │
│  │  │ │ description   VARCHAR(255) NOT NULL             │ │  │ │
│  │  │ │ amount        DECIMAL(12,2) NOT NULL            │ │  │ │
│  │  │ │ category      VARCHAR(100) NOT NULL             │ │  │ │
│  │  │ │ date          DATE NOT NULL                     │ │  │ │
│  │  │ │ created_at    DATETIME DEFAULT CURRENT_TIMESTAMP│ │  │ │
│  │  │ │ updated_at    DATETIME ON UPDATE CURRENT_TIMESTAMP│ │  │ │
│  │  │ └─────────────────────────────────────────────────┘ │  │ │
│  │  └─────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow Diagram

### Adding an Expense (Create Operation)

```
User                Frontend              Backend              Database
 │                     │                     │                     │
 │  1. Fill form &     │                     │                     │
 │     click "Add"     │                     │                     │
 │─────────────────────>                     │                     │
 │                     │                     │                     │
 │                     │  2. POST Request    │                     │
 │                     │  /api/expenses      │                     │
 │                     │  Body: {            │                     │
 │                     │   description,      │                     │
 │                     │   amount,           │                     │
 │                     │   category,         │                     │
 │                     │   date              │                     │
 │                     │  }                  │                     │
 │                     │─────────────────────>                     │
 │                     │                     │                     │
 │                     │                     │  3. Validate &      │
 │                     │                     │     Process         │
 │                     │                     │                     │
 │                     │                     │  4. INSERT INTO     │
 │                     │                     │     expenses        │
 │                     │                     │─────────────────────>
 │                     │                     │                     │
 │                     │                     │  5. Return ID       │
 │                     │                     │<─────────────────────
 │                     │                     │                     │
 │                     │  6. Response 201    │                     │
 │                     │  Created            │                     │
 │                     │  Body: {            │                     │
 │                     │   id: 1,            │                     │
 │                     │   description,      │                     │
 │                     │   amount,           │                     │
 │                     │   category,         │                     │
 │                     │   date              │                     │
 │                     │  }                  │                     │
 │                     │<─────────────────────                     │
 │                     │                     │                     │
 │  7. Update UI       │                     │                     │
 │     Add to table    │                     │                     │
 │     Update total    │                     │                     │
 │<─────────────────────                     │                     │
 │                     │                     │                     │
```

### Loading Expenses (Read Operation)

```
User                Frontend              Backend              Database
 │                     │                     │                     │
 │  1. Open page /     │                     │                     │
 │     Refresh         │                     │                     │
 │─────────────────────>                     │                     │
 │                     │                     │                     │
 │                     │  2. GET Request     │                     │
 │                     │  /api/expenses      │                     │
 │                     │─────────────────────>                     │
 │                     │                     │                     │
 │                     │                     │  3. SELECT *        │
 │                     │                     │     FROM expenses   │
 │                     │                     │─────────────────────>
 │                     │                     │                     │
 │                     │                     │  4. Return rows     │
 │                     │                     │<─────────────────────
 │                     │                     │                     │
 │                     │  5. Response 200    │                     │
 │                     │  Body: [            │                     │
 │                     │   {expense1},       │                     │
 │                     │   {expense2},       │                     │
 │                     │   ...               │                     │
 │                     │  ]                  │                     │
 │                     │<─────────────────────                     │
 │                     │                     │                     │
 │  6. Render table    │                     │                     │
 │     Calculate total │                     │                     │
 │<─────────────────────                     │                     │
 │                     │                     │                     │
```

## Technology Stack

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend Layer                           │
├─────────────────────────────────────────────────────────────┤
│ • HTML5 - Structure                                         │
│ • CSS3 - Styling                                            │
│ • JavaScript (Vanilla) - Logic & AJAX                       │
│ • Fetch API - HTTP Requests                                 │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   Backend Layer                             │
├─────────────────────────────────────────────────────────────┤
│ • Java 17 - Programming Language                            │
│ • Spring Boot 3.2.0 - Framework                             │
│ • Spring Web - REST API                                     │
│ • Spring Data JPA - Data Access                             │
│ • Spring Security - Authentication                          │
│ • Hibernate - ORM                                           │
│ • Maven - Build Tool                                        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   Database Layer                            │
├─────────────────────────────────────────────────────────────┤
│ • MySQL 8.0 - Relational Database                           │
│ • InnoDB Engine - Storage                                   │
│ • JDBC - Database Connectivity                              │
└─────────────────────────────────────────────────────────────┘
```

## Testing Strategy

```
┌──────────────────────────────────────────────────────────────┐
│                      Testing Pyramid                         │
│                                                              │
│                           ▲                                  │
│                          ╱ ╲                                 │
│                         ╱   ╲                                │
│                        ╱ E2E ╲                               │
│                       ╱ Tests ╲                              │
│                      ╱─────────╲                             │
│                     ╱           ╲                            │
│                    ╱ Integration ╲                           │
│                   ╱     Tests     ╲                          │
│                  ╱─────────────────╲                         │
│                 ╱                   ╲                        │
│                ╱    Unit Tests       ╲                       │
│               ╱                       ╲                      │
│              ╱─────────────────────────╲                     │
│                                                              │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Unit Tests (src/test/java/...)                             │
│  • ExpenseServiceTest                                        │
│  • ExpenseRepositoryTest                                     │
│  • Uses H2 in-memory database                                │
│                                                              │
│  Integration Tests (test-integration.ps1)                    │
│  • API endpoint testing                                      │
│  • Backend ↔ MySQL integration                               │
│  • CRUD operations validation                                │
│                                                              │
│  E2E Tests (Manual via Frontend)                             │
│  • Complete user workflows                                   │
│  • Frontend ↔ Backend ↔ MySQL                                │
│  • Cross-browser testing                                     │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

## Configuration Overview

### Development Configuration

```
┌────────────────────────────────────────────┐
│ application.properties                     │
├────────────────────────────────────────────┤
│ Database:                                  │
│   URL: localhost:3306/expense_db           │
│   User: expense_user                       │
│   Password: expense_password               │
│                                            │
│ JPA:                                       │
│   DDL: update (auto-create tables)         │
│   Show SQL: true (debug)                   │
│   Dialect: MySQL                           │
│                                            │
│ Server:                                    │
│   Port: 8080                               │
│   Context Path: /                          │
│                                            │
│ Security:                                  │
│   Basic Auth (temporary)                   │
│   CORS: enabled (all origins)              │
└────────────────────────────────────────────┘
```

### Test Configuration

```
┌────────────────────────────────────────────┐
│ application-test.properties                │
├────────────────────────────────────────────┤
│ Database:                                  │
│   Type: H2 In-Memory                       │
│   Mode: MySQL Compatibility                │
│   DDL: create-drop                         │
│                                            │
│ JPA:                                       │
│   Show SQL: true                           │
│   Dialect: H2                              │
│                                            │
│ Server:                                    │
│   Port: 8080                               │
└────────────────────────────────────────────┘
```

## Deployment Considerations

### Production Architecture (Future)

```
                        Internet
                            │
                            ▼
                    ┌─────────────┐
                    │ Load Balan. │
                    └─────────────┘
                            │
            ┌───────────────┼───────────────┐
            ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │ Backend #1  │ │ Backend #2  │ │ Backend #3  │
    │ (Spring)    │ │ (Spring)    │ │ (Spring)    │
    └─────────────┘ └─────────────┘ └─────────────┘
            │               │               │
            └───────────────┼───────────────┘
                            ▼
                    ┌─────────────┐
                    │ MySQL RDS   │
                    │ (Primary +  │
                    │  Replica)   │
                    └─────────────┘

Frontend: CDN (CloudFront, Cloudflare)
Backend: EC2, ECS, or App Service
Database: RDS, Aurora, or Azure Database
```

## Security Considerations

```
Current (Development):
├── CORS: Open (all origins) ⚠️
├── Authentication: Basic Auth
├── Validation: Server-side only
└── HTTPS: Not implemented

Production (Required):
├── CORS: Specific origins only
├── Authentication: JWT or OAuth2
├── Validation: Client + Server
├── HTTPS: Required (SSL/TLS)
├── SQL Injection: Prepared statements (JPA)
├── XSS Protection: Input sanitization
├── Rate Limiting: API throttling
└── Environment Variables: Secrets management
```

---

## Summary

This architecture provides:
- ✅ **Separation of Concerns** - Frontend, Backend, Database layers
- ✅ **RESTful API** - Standard HTTP methods and status codes
- ✅ **Data Persistence** - MySQL relational database
- ✅ **ORM Integration** - JPA/Hibernate for database abstraction
- ✅ **Scalability** - Can add load balancing and horizontal scaling
- ✅ **Testability** - Unit, integration, and E2E testing
- ✅ **Maintainability** - Clear project structure and documentation
