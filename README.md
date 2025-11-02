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

| Member | Role | Responsibilities |
|--------|------|-----------------|
| **Duane Mitchell** | Project Manager / Backend Developer | Project oversight, milestone management, system integration, backend implementation |
| **John Malone** | Frontend Lead | UI/UX design, responsive features, visual interface management |
| **Eric Gray** | Backend Developer (Java) | Core Java modules, data handling, backend services integration |
| **Michael Basye** | Database Engineer | MySQL schema design, query optimization, data reliability |
| **Pukar Adhikari** | Frontend Developer / Tester | UI functionality testing, bug tracking |
| **James Strange** | QA Lead / Documentation Specialist | Testing documentation, quality control, report compilation |

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
expense-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/yourapp/expensetracker/expense_api/
│   │   │       ├── ExpenseApiApplication.java          # Main application entry point
│   │   │       ├── config/
│   │   │       │   └── SecurityConfig.java             # Security configuration
│   │   │       ├── controller/
│   │   │       │   ├── AuthController.java             # Authentication endpoints
│   │   │       │   ├── ExpenseController.java          # Expense CRUD operations
│   │   │       │   └── ReportController.java           # Report generation
│   │   │       ├── model/
│   │   │       │   └── expense.java                    # Expense entity model
│   │   │       ├── repository/
│   │   │       │   └── ExpenseRepository.java          # Data access layer
│   │   │       └── service/
│   │   │           └── ExpenseService.java             # Business logic layer
│   │   └── resources/
│   │       ├── application.properties                  # Application configuration
│   │       ├── static/                                 # Static web resources
│   │       └── templates/                              # View templates
│   └── test/
│       └── java/
│           └── com/yourapp/expensetracker/expense_api/
│               └── ExpenseApiApplicationTests.java     # Unit tests
├── target/                                             # Compiled classes and artifacts
├── pom.xml                                             # Maven configuration
├── mvnw                                                # Maven wrapper (Unix)
├── mvnw.cmd                                            # Maven wrapper (Windows)
└── README.md                                           # Project documentation
```

## 📋 Prerequisites

Before running this application, ensure you have the following installed:

- **Java Development Kit (JDK) 21+**
- **MySQL 8.0+**
- **Maven 3.6+** (or use included Maven wrapper)
- **Git** (for version control)

### Recommended IDEs
- IntelliJ IDEA (Community or Ultimate Edition)
- Visual Studio Code with Java Extension Pack
- Eclipse IDE for Java Developers

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/your-team/smart-expense-tracker.git
cd smart-expense-tracker/expense-api
```

### 2. Database Setup
1. Install and start MySQL server
2. Create a new database:
```sql
CREATE DATABASE expense_db;
CREATE USER 'expense_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON expense_db.* TO 'expense_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure Application Properties
Update `src/main/resources/application.properties`:
```properties
spring.application.name=expense-api

# MySQL Database Connection
spring.datasource.url=jdbc:mysql://localhost:3306/expense_db
spring.datasource.username=expense_user
spring.datasource.password=your_password

# JPA/Hibernate Settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

## ⚙️ Configuration

### Development Environment
- The application uses `spring.jpa.hibernate.ddl-auto=update` for automatic schema updates
- SQL queries are logged when `spring.jpa.show-sql=true`
- Default server port is 8080

### Production Considerations
- Change `ddl-auto` to `validate` or `none` in production
- Configure proper logging levels
- Set up environment-specific profiles
- Configure SSL/TLS for secure connections

## 🏃‍♂️ Running the Application

### Using Maven Wrapper (Recommended)
```bash
# On Windows
./mvnw.cmd spring-boot:run

# On Unix/Linux/macOS
./mvnw spring-boot:run
```

### Using Maven Directly
```bash
mvn spring-boot:run
```

### Using IDE
1. Import the project as a Maven project
2. Run `ExpenseApiApplication.java` as a Java application

The application will start on `http://localhost:8080`

## 🔌 API Endpoints

### Expense Management
| Method | Endpoint | Description | Status |
|--------|----------|-------------|---------|
| `POST` | `/api/expenses` | Create a new expense | 🚧 In Development |
| `GET` | `/api/expenses` | Get all expenses for user | 🚧 In Development |
| `GET` | `/api/expenses/{id}` | Get expense by ID | 📝 Planned |
| `PUT` | `/api/expenses/{id}` | Update expense | 🚧 In Development |
| `DELETE` | `/api/expenses/{id}` | Delete expense | 📝 Planned |

### Authentication (Planned)
| Method | Endpoint | Description | Status |
|--------|----------|-------------|---------|
| `POST` | `/api/auth/login` | User login | 📝 Planned |
| `POST` | `/api/auth/register` | User registration | 📝 Planned |
| `POST` | `/api/auth/logout` | User logout | 📝 Planned |

### Reports (Planned)
| Method | Endpoint | Description | Status |
|--------|----------|-------------|---------|
| `GET` | `/api/reports/summary` | Get expense summary | 📝 Planned |
| `GET` | `/api/reports/category` | Get category breakdown | 📝 Planned |

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
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ExpenseApiApplicationTests
```

### Test Coverage
- Unit tests for service layer components
- Integration tests for API endpoints
- Database interaction tests
- Security configuration tests

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

| Phase | Timeline | Status |
|-------|----------|---------|
| **Planning & Design** | Weeks 1-2 | ✅ Complete |
| **Backend/Database Development** | Weeks 3-4 | 🚧 In Progress |
| **Frontend Development** | Weeks 4-5 | 📝 Upcoming |
| **Testing & Debugging** | Weeks 5-7 | 📝 Upcoming |
| **Documentation** | Weeks 6-7 | 📝 Upcoming |
| **Final Presentation** | Week 8 | 📝 Upcoming |

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

## 📞 Support

For questions or support, please contact the development team through our established communication channels or create an issue in the GitHub repository.

---

**University of Maryland Global Campus**  
**CMSC 495: Computer Science Capstone**  
**Fall 2025 - Group 3**

*Last Updated: October 31, 2025*