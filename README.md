# Smart Expense Tracking Application

A web-based expense tracking application developed as a capstone project for CMSC 495 at the University of Maryland Global Campus. This application enables users to record, categorize, and analyze their daily financial transactions with secure authentication and intuitive data visualization.

## 📋 Table of Contents

- [Overview](#overview)
- [Team](#team)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Usage](#usage)
- [Testing](#testing)
- [Contributing](#contributing)
- [Project Timeline](#project-timeline)
- [License](#license)

## 🎯 Overview

The Smart Expense Tracking Application provides a comprehensive solution for personal financial management. Users can securely log in, enter expenses with detailed categorization, and view their spending patterns through visual analytics including charts and reports.

### Key Objectives

- Provide a simple and secure interface for managing daily expenses
- Enable users to track, categorize, and analyze spending trends
- Support data visualization through charts and summaries
- Maintain secure user authentication and data protection

## 👥 Team

**Group 3 - UMGC CMSC 495 Capstone Project**

| Member             | Role                                | Responsibilities                                                                    |
| ------------------ | ----------------------------------- | ----------------------------------------------------------------------------------- |
| **Duane Mitchell** | Project Manager / Backend Developer | Project oversight, milestone management, system integration, backend implementation |
| **John Malone**    | Frontend Lead                       | UI/UX design, responsive features, visual interface management                      |
| **Eric Gray**      | Backend Developer (Java)            | Core Java modules, data handling, backend services integration                      |
| **Michael Basye**  | Database Engineer                   | MySQL schema design, query optimization, data reliability                           |
| **Pukar Adhikari** | Frontend Developer / Tester         | UI functionality testing, bug tracking                                              |
| **James Strange**  | QA Lead / Documentation Specialist  | Testing documentation, quality control, report compilation                          |

## ✨ Features

### Current Features

- **Secure Authentication System**: User login/logout with Spring Security
- **Expense Management**: Add, edit, and delete expense entries
- **Expense Categorization**: Organize expenses by type (food, transportation, utilities, etc.)
- **RESTful API**: Clean API endpoints for expense operations
- **Database Integration**: MySQL integration with JPA/Hibernate

### Planned Features

- **Visual Analytics**: Charts and graphs for spending visualization
- **Report Generation**: Exportable summary reports (CSV/PDF format)
- **Administrative Dashboard**: User management capabilities
- **Responsive Web Interface**: Mobile-friendly design
- **Data Summaries**: Monthly and category-based expense summaries

## 🛠 Technology Stack

### Backend

- **Java 21** - Core programming language
- **Spring Boot 4.0.0-RC1** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **Hibernate** - ORM framework

### Database

- **MySQL** - Primary database
- **MySQL Connector/J** - Database driver

### Frontend (Planned)

- **HTML5** - Markup language
- **CSS3** - Styling and responsive design
- **JavaScript** - Client-side interactivity

### Development Tools

- **Maven** - Build automation and dependency management
- **Git/GitHub** - Version control and collaboration
- **IntelliJ IDEA / VS Code** - Development environments

## 📁 Project Structure

```
SmartExpenseTrackingApp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/yourapp/expensetracker/expense_api/
│   │   │       ├── ExpenseApiApplication.java               # Main Spring Boot application entry point
│   │   │       │
│   │   │       ├── config/
│   │   │       │   └── SecurityConfig.java                  # Spring Security configuration (CORS, CSRF, endpoints)
│   │   │       │
│   │   │       ├── controller/                              # REST API controllers
│   │   │       │   ├── AuthController.java                  # Authentication endpoints (placeholder for future JWT/auth logic)
│   │   │       │   ├── ExpenseController.java               # Expense CRUD operations and filters
│   │   │       │   └── ReportController.java                # Expense reporting and analytics endpoints
│   │   │       │
│   │   │       ├── model/                                   # JPA entity models
│   │   │       │   ├── Expense.java                         # Expense entity (transactions)
│   │   │       │   ├── User.java                            # User entity (authentication and ownership)
│   │   │       │   ├── Budget.java                          # Budget entity (planned spending limits)
│   │   │       │   └── Category.java                        # Category entity (expense types)
│   │   │       │
│   │   │       ├── repository/                              # Data access layer (JPA repositories)
│   │   │       │   ├── ExpenseRepository.java               # Expense data operations
│   │   │       │   ├── UserRepository.java                  # User data operations
│   │   │       │   ├── BudgetRepository.java                # Budget data operations
│   │   │       │   └── CategoryRepository.java              # Category data operations
│   │   │       │
│   │   │       └── service/                                 # Business logic layer
│   │   │           ├── ExpenseService.java                  # Handles expense-related logic
│   │   │           ├── BudgetService.java                   # Handles budget management logic
│   │   │           ├── ReportService.java                   # Generates reports and summaries
│   │   │           └── CategoryService.java                 # Suggests and manages categories
│   │   │
│   │   └── resources/
│   │       ├── application.properties                       # Database and app configuration (MySQL, Hibernate, etc.)
│   │       ├── static/                                      # Static web assets (if used by frontend)
│   │       └── templates/                                   # Thymeleaf or view templates (future use)
│   │
│   └── test/
│       └── java/
│           └── com/yourapp/expensetracker/expense_api/
│               └── ExpenseApiApplicationTests.java          # Unit and integration tests
│
├── target/                                                  # Compiled class files and build artifacts
├── pom.xml                                                  # Maven configuration and dependencies
├── mvnw                                                     # Maven wrapper (Unix)
├── mvnw.cmd                                                 # Maven wrapper (Windows)
└── README.md                                                # Project documentation```

## 📋 Prerequisites

Before running this application, ensure you have the following installed:

- **Docker Desktop** - For running MySQL database ([Download](https://www.docker.com/products/docker-desktop/))
- **Java Development Kit (JDK) 17+**
- **Maven 3.6+** (or use included Maven wrapper)
- **Git** (for version control)

### Recommended IDEs

- IntelliJ IDEA (Community or Ultimate Edition)
- Visual Studio Code with Java Extension Pack
- Eclipse IDE for Java Developers

## 🚀 Quick Start with Docker

### 1. Clone the Repository

```bash
git clone https://github.com/dmitc072/SmartExpenseTrackingApp.git
cd SmartExpenseTrackingApp
```

### 2. Start MySQL Database (Docker)

```powershell
# Start MySQL container with Docker Compose
docker compose up -d

# Verify MySQL is running
docker ps
```

**What this does:**
- Downloads MySQL 8.0 image (first time only)
- Creates `expense_db` database
- Creates `expense_user` with password `expense_password`
- Initializes database schema automatically from `database/schema.sql`
- Runs MySQL on port 3306

**Alternative:** Use the interactive setup script:
```powershell
.\setup-docker.ps1
# Choose option 1
```

### 3. Start Backend Server

```powershell
# Using Maven wrapper (recommended)
.\mvnw.cmd spring-boot:run

# Or using Maven directly
mvn spring-boot:run
```

Wait for: `Started ExpenseApiApplication in X.XXX seconds`

### 4. Test the Integration

```powershell
# Run automated integration tests
.\test-integration.ps1

# Or manually test the frontend
start frontend\dashboard.html
```

---

## 📚 Detailed Setup Guides

- **Docker Setup**: See [README_DOCKER.md](README_DOCKER.md) for complete Docker guide
- **Team Onboarding**: See [TEAM_ONBOARDING.md](TEAM_ONBOARDING.md) for new team member setup
- **Architecture**: See [ARCHITECTURE.md](ARCHITECTURE.md) for system architecture details

## ⚙️ Configuration

### Development Environment

The application is pre-configured to work with Docker MySQL:

- **Database URL**: `jdbc:mysql://localhost:3306/expense_db`
- **Username**: `expense_user`
- **Password**: `expense_password`
- **Server Port**: 8080

Configuration file: `src/main/resources/application.properties`

### Docker Commands

```powershell
# Start MySQL
docker compose up -d

# Stop MySQL (keeps data)
docker compose stop

# Restart MySQL
docker compose start

# Stop and remove (fresh start)
docker compose down -v && docker compose up -d

# View logs
docker compose logs -f
```

### Running the Backend

```powershell
# Using Maven wrapper (recommended)
.\mvnw.cmd spring-boot:run

# Using IDE
# Import as Maven project and run ExpenseApiApplication.java
```

The application will start on `http://localhost:8080`

### Frontend Access

Open `frontend/dashboard.html` in your browser or:
```powershell
start frontend\dashboard.html
```

## 🔌 API Endpoints

### Expense Management

| Method   | Endpoint             | Description               | Status            |
| -------- | -------------------- | ------------------------- | ----------------- |
| `POST`   | `/api/expenses`      | Create a new expense      | 🚧 In Development |
| `GET`    | `/api/expenses`      | Get all expenses for user | 🚧 In Development |
| `GET`    | `/api/expenses/{id}` | Get expense by ID         | 📝 Planned        |
| `PUT`    | `/api/expenses/{id}` | Update expense            | 🚧 In Development |
| `DELETE` | `/api/expenses/{id}` | Delete expense            | 📝 Planned        |

### Authentication (Planned)

| Method | Endpoint             | Description       | Status     |
| ------ | -------------------- | ----------------- | ---------- |
| `POST` | `/api/auth/login`    | User login        | 📝 Planned |
| `POST` | `/api/auth/register` | User registration | 📝 Planned |
| `POST` | `/api/auth/logout`   | User logout       | 📝 Planned |

### Reports (Planned)

| Method | Endpoint                | Description            | Status     |
| ------ | ----------------------- | ---------------------- | ---------- |
| `GET`  | `/api/reports/summary`  | Get expense summary    | 📝 Planned |
| `GET`  | `/api/reports/category` | Get category breakdown | 📝 Planned |

## 📱 Usage

### User Walkthrough

1. **Welcome Screen**: User accesses login or registration
2. **Dashboard**: View recent expenses and access "Add Expense" functionality
3. **Add Expense**: Fill form with amount, category, date, and description
4. **View Expenses**: Browse transaction history with color-coded categories
5. **Reports**: Access visual analytics and spending summaries
6. **Account Management**: Edit or delete expense entries as needed

### Example Request (Future Implementation)

```json
POST /api/expenses
Content-Type: application/json

{
  "description": "Grocery shopping",
  "amount": 85.50,
  "category": "Food",
  "date": "2025-10-31"
}
```

## 🧪 Testing

### Running Tests

The application supports two testing modes:

#### 1. Fast Unit Tests (H2 Database - Default)
```powershell
# Run all tests with H2 in-memory database
.\mvnw.cmd test

# Run specific test class
.\mvnw.cmd test -Dtest=ExpenseApiApplicationTests
```
**Best for**: Fast feedback during development, CI/CD pipelines

#### 2. Integration Tests (Docker MySQL)
```powershell
# Ensure Docker MySQL is running first
docker ps

# Run all tests with Docker MySQL
.\mvnw.cmd test "-Dspring.profiles.active=docker"
```
**Best for**: End-to-end integration testing, pre-deployment validation

### Test Coverage

- **49 Integration Tests** covering all application layers:
  - **Controller Layer**: 13 tests for REST API endpoints
  - **Service Layer**: 13 tests for business logic
  - **Repository Layer**: 12 tests for database operations
  - **Report Service**: 10 tests for analytics features
  - **Application Context**: 1 smoke test

### Test Configuration

- **Default Profile (`test`)**: Uses H2 in-memory database
  - Location: `src/test/resources/application-test.properties`
  - Automatic schema generation
  - Isolated test data for each test class
  
- **Docker Profile (`docker`)**: Uses Docker MySQL database
  - Location: `src/test/resources/application-docker.properties`
  - Tests against real MySQL database
  - Requires Docker container to be running

## 🤝 Contributing

### Development Workflow

1. Create a feature branch from `main`
2. Implement changes with appropriate tests
3. Submit pull request for review
4. Merge after approval and testing

### Code Standards

- Follow Java naming conventions
- Document public methods with Javadoc
- Maintain consistent formatting
- Write comprehensive unit tests

### Communication Channels

- **Discord**: Daily communication and quick questions
- **Microsoft Teams**: Weekly meetings and screen-sharing
- **GitHub Issues**: Bug tracking and feature requests
- **GitHub Projects**: Task management and progress tracking

## 📅 Project Timeline

| Phase                            | Timeline  | Status         |
| -------------------------------- | --------- | -------------- |
| **Planning & Design**            | Weeks 1-2 | ✅ Complete    |
| **Backend/Database Development** | Weeks 3-4 | 🚧 In Progress |
| **Frontend Development**         | Weeks 4-5 | 📝 Upcoming    |
| **Testing & Debugging**          | Weeks 5-7 | 📝 Upcoming    |
| **Documentation**                | Weeks 6-7 | 📝 Upcoming    |
| **Final Presentation**           | Week 8    | 📝 Upcoming    |

### Key Milestones

- ✅ Project planning and team role assignment
- ✅ Initial Spring Boot application setup
- 🚧 Database schema and basic API endpoints
- 📝 Frontend interface development
- 📝 User authentication implementation
- 📝 Data visualization and reporting features
- 📝 Comprehensive testing and bug fixes
- 📝 Final documentation and presentation

## 📄 License

This project is developed as an academic assignment for UMGC CMSC 495. All rights reserved to the contributing team members.

## 🧪 Integration Testing

### Dual Testing Strategy

The application provides two testing modes for different use cases:

#### Fast Unit Tests (H2 Database)
```powershell
# Run all tests with H2 in-memory database (default)
.\mvnw.cmd test

# Run with detailed output
.\mvnw.cmd test -X

# Run specific test class
.\mvnw.cmd test -Dtest=ExpenseControllerIntegrationTest
```
✅ **Use for**: Fast feedback loops, CI/CD pipelines, development

#### Docker Integration Tests (MySQL)
```powershell
# Ensure Docker MySQL is running
docker ps --filter "name=expense-tracker-mysql"

# Run all tests against Docker MySQL
.\mvnw.cmd test "-Dspring.profiles.active=docker"
```
✅ **Use for**: End-to-end validation, pre-deployment testing, realistic scenarios

### Test Coverage Summary

- **49 Integration Tests** covering all application layers
- **Controller Layer**: 13 tests for REST API endpoints
- **Service Layer**: 13 tests for business logic  
- **Repository Layer**: 12 tests for database operations
- **Report Service**: 10 tests for analytics features
- **Application Context**: 1 smoke test

### Test Configuration

**H2 Profile (Default - `test`)**:
- Configuration: `src/test/resources/application-test.properties`
- Database: H2 in-memory
- Schema: Auto-generated from JPA entities
- Isolation: Each test class gets clean database

**Docker Profile (`docker`)**:
- Configuration: `src/test/resources/application-docker.properties`
- Database: Docker MySQL (localhost:3306)
- Schema: Auto-generated on Docker MySQL
- Isolation: Shared database, cleaned between test classes

### Running Integration Tests Script

For comprehensive frontend + backend + database testing:
```powershell
# Run automated integration test script
.\test-integration.ps1
```
This script tests the complete stack including REST API endpoints.

## � Report API Endpoints

The application provides comprehensive reporting endpoints:

### Analytics Endpoints

- `GET /api/reports/yearly?year=2025` - Yearly expense report
- `GET /api/reports/monthly?year=2025&month=10` - Monthly report
- `GET /api/reports/category-breakdown?startDate=...&endDate=...` - Category breakdown
- `GET /api/reports/total?startDate=...&endDate=...` - Total expenses
- `GET /api/reports/top-categories?startDate=...&endDate=...&limit=5` - Top spending categories
- `GET /api/reports/average-daily?startDate=...&endDate=...` - Average daily expenses
- `GET /api/reports/compare-months?year1=...&month1=...&year2=...&month2=...` - Month comparison
- `GET /api/reports/weekly-trends?startDate=...&endDate=...` - Weekly spending trends

For complete endpoint documentation, see the API reference above.

## �📞 Support

For questions or support, please contact the development team through our established communication channels or create an issue in the GitHub repository.

---

**University of Maryland Global Campus**  
**CMSC 495: Computer Science Capstone**  
**Fall 2025 - Group 3**

_Last Updated: November 9, 2025_
