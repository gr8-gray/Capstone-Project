# Error Handling and Logging Implementation

**Author**: Eric Gray - Backend Developer  
**Date**: November 15, 2025  
**Project**: Smart Expense Tracking App - UMGC CMSC 495 Capstone

## Overview

Comprehensive error handling and logging has been implemented across both backend (Java/Spring Boot) and frontend (JavaScript) to improve debugging, monitoring, and user experience.

---

## Backend Enhancements

### 1. SLF4J Logging Implementation

#### Controllers Enhanced
- **AuthController**: Login, registration, profile retrieval logging
- **ExpenseController**: CRUD operation logging with IDs and amounts

#### Services Enhanced
- **AuthService**: Detailed authentication flow logging
  - Registration attempts with username tracking
  - Login attempts with success/failure tracking
  - Token generation logging
  - User not found scenarios

#### Log Levels Used
- `INFO`: Successful operations (login, registration, expense creation)
- `WARN`: Failed operations, validation errors, unauthorized access
- `DEBUG`: Internal operations, token validation
- `ERROR`: Unexpected exceptions with stack traces

### 2. Global Exception Handler Improvements

**File**: `GlobalExceptionHandler.java`

Enhanced with:
- Structured error responses with timestamps
- Authentication exception handling (BadCredentialsException)
- Comprehensive logging for all exception types
- Field-level validation error details
- Runtime exception categorization

### 3. Error Response DTO

**File**: `ErrorResponse.java`

Standardized error response structure:
```json
{
  "timestamp": "2025-11-15T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/expenses",
  "details": ["amount must be positive"]
}
```

### 4. Application Properties Configuration

**File**: `application.properties`

Added logging configuration:
- Console and file logging patterns
- Log file rotation (10MB max size, 30 days retention)
- Log directory: `logs/expense-tracker.log`
- Structured format with timestamps and thread info

```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n
logging.file.name=logs/expense-tracker.log
logging.file.max-size=10MB
logging.file.max-history=30
```

---

## Frontend Enhancements

### 1. Logging Utility Module

**File**: `auth.js`

New logging functions:
- `log(level, message, data)`: Timestamped console logging
- `showError(message)`: User-friendly error display
- `showSuccess(message)`: Success message display

Log levels: `ERROR`, `WARN`, `INFO`, `DEBUG`

### 2. Enhanced API Error Handling

#### authenticatedFetch() Improvements
- Network error detection (backend down scenarios)
- 401 Unauthorized handling with user notification
- 500+ Server error detection
- Detailed error logging with request URLs

#### Authentication Functions
- `register()`: Network error handling, validation error display
- `login()`: Credential error handling, backend connectivity checks
- `getCurrentUserProfile()`: Session expiry handling

### 3. Expense Management Error Handling

**File**: `script.js`

Enhanced functions:
- `fetchExpenses()`: Connection error messaging
- `addExpense()`: Validation error extraction from responses
- `updateExpense()`: Update failure logging with expense IDs
- `deleteExpense()`: Delete operation error handling

Error messages include:
- "Unable to connect to server. Please ensure the backend is running on http://localhost:8080"
- "Failed to load expenses. Please try again later."
- HTTP status codes in error messages

---

## Error Handling Patterns

### Backend Pattern
```java
logger.info("Operation started: {}", param);
try {
    // Operation
    logger.info("Operation successful");
    return result;
} catch (SpecificException e) {
    logger.warn("Expected error: {}", e.getMessage());
    throw e;
} catch (Exception e) {
    logger.error("Unexpected error: {}", e.getMessage(), e);
    throw new RuntimeException("Operation failed", e);
}
```

### Frontend Pattern
```javascript
log(LOG_LEVELS.INFO, 'Operation started');
try {
    const response = await fetch(url);
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Operation failed');
    }
    log(LOG_LEVELS.INFO, 'Operation successful');
    return await response.json();
} catch (error) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
        log(LOG_LEVELS.ERROR, 'Network error', error);
        throw new Error('Unable to connect to server');
    }
    log(LOG_LEVELS.ERROR, 'Operation error', error);
    throw error;
}
```

---

## Benefits

### 1. **Debugging**
- Detailed logs with timestamps and context
- Stack traces for unexpected errors
- Request/response tracking

### 2. **Monitoring**
- Log file rotation for long-term analysis
- Structured log format for parsing
- Operation success/failure metrics

### 3. **User Experience**
- Friendly error messages (no technical jargon)
- Specific guidance (e.g., "check if backend is running")
- Session expiry notifications

### 4. **Security**
- Sensitive data not logged (passwords filtered)
- Generic error messages to users
- Detailed errors only in logs

---

## Testing Error Scenarios

### 1. Backend Down
- Frontend shows: "Unable to connect to server"
- Console logs network error with URL

### 2. Invalid Credentials
- Backend logs: "Login failed - invalid credentials"
- User sees: "Invalid username/email or password"

### 3. Token Expiry
- Backend logs: "Authentication expired"
- User redirected to login with message

### 4. Validation Error
- Backend logs field-level errors
- Frontend displays specific field errors
- 400 Bad Request with details

### 5. Server Error
- Backend logs full stack trace
- User sees: "Server error. Please try again later."
- Error details logged but not exposed

---

## Log File Locations

- **Backend Logs**: `logs/expense-tracker.log`
- **Frontend Logs**: Browser console (F12 Developer Tools)

---

## Future Improvements

1. **Structured Logging**: JSON format for log aggregation tools
2. **Log Correlation IDs**: Track requests across services
3. **Metrics**: Response times, error rates
4. **Alerting**: Email/Slack notifications for critical errors
5. **Frontend Error Reporting**: Send errors to backend for tracking

---

## Summary

✅ **Backend**: SLF4J logging in all controllers and services  
✅ **Frontend**: Console logging with timestamps and levels  
✅ **Error Responses**: Standardized ErrorResponse DTO  
✅ **Exception Handling**: Global handler with comprehensive coverage  
✅ **User Experience**: Friendly error messages with guidance  
✅ **Log Files**: Rotating file appender with 30-day retention  
✅ **Network Errors**: Specific handling for backend connectivity issues  
✅ **Authentication**: Session expiry and credential error handling  

All error scenarios are now properly logged and handled with appropriate user feedback!
