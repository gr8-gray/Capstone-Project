# User Authentication System

## Overview
The Smart Expense Tracker now includes a complete JWT-based authentication system with user registration, login, and protected API endpoints.

## Architecture

### Components
- **JWT Token Provider**: Generates and validates JWT tokens
- **Custom User Details Service**: Loads user information from database
- **JWT Authentication Filter**: Intercepts requests and validates tokens
- **Auth Service**: Handles registration and login logic
- **User Service**: Manages user CRUD operations
- **Auth Controller**: REST endpoints for authentication

## API Endpoints

### Authentication Endpoints

#### 1. Register New User
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123"
}

Response (201 Created):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "role": "USER"
}
```

#### 2. Login
```
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "johndoe",
  "password": "password123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "role": "USER"
}
```

#### 3. Get Current User Profile
```
GET /api/auth/me
Authorization: Bearer <token>

Response (200 OK):
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "role": "USER",
  "enabled": true,
  "createdAt": "2025-11-15T10:30:00",
  "updatedAt": "2025-11-15T10:30:00"
}
```

#### 4. Logout
```
POST /api/auth/logout

Response (200 OK):
{
  "message": "Logged out successfully"
}

Note: JWT is stateless, so logout is handled client-side by removing the token
```

#### 5. Check Username Availability
```
GET /api/auth/check-username?username=johndoe

Response (200 OK):
{
  "exists": true
}
```

#### 6. Check Email Availability
```
GET /api/auth/check-email?email=john@example.com

Response (200 OK):
{
  "exists": true
}
```

## Protected Endpoints

All expense-related endpoints now require authentication:
- `/api/expenses/**` - All expense operations
- `/api/budgets/**` - All budget operations
- `/api/reports/**` - All report operations

### Making Authenticated Requests

Include the JWT token in the Authorization header:
```
GET /api/expenses
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Frontend Integration

### 1. Login Flow
```javascript
async function login(usernameOrEmail, password) {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ usernameOrEmail, password })
  });
  
  const data = await response.json();
  
  if (response.ok) {
    // Store token in localStorage
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify({
      id: data.userId,
      username: data.username,
      email: data.email,
      role: data.role
    }));
    return data;
  } else {
    throw new Error(data.error);
  }
}
```

### 2. Register Flow
```javascript
async function register(username, email, password) {
  const response = await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, email, password })
  });
  
  const data = await response.json();
  
  if (response.ok) {
    // Store token in localStorage
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify({
      id: data.userId,
      username: data.username,
      email: data.email,
      role: data.role
    }));
    return data;
  } else {
    throw new Error(data.error);
  }
}
```

### 3. Making Authenticated API Calls
```javascript
async function fetchExpenses() {
  const token = localStorage.getItem('token');
  
  const response = await fetch('http://localhost:8080/api/expenses', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (response.status === 401) {
    // Token expired or invalid - redirect to login
    window.location.href = 'login.html';
    return;
  }
  
  return await response.json();
}
```

### 4. Logout Flow
```javascript
function logout() {
  // Remove token from localStorage
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  
  // Redirect to login page
  window.location.href = 'login.html';
}
```

### 5. Check Authentication Status
```javascript
function isAuthenticated() {
  const token = localStorage.getItem('token');
  return token !== null;
}

// Protect pages that require authentication
if (!isAuthenticated()) {
  window.location.href = 'login.html';
}
```

## Security Features

### Password Security
- Passwords are hashed using BCrypt with strength 12
- Plain text passwords are never stored in the database

### JWT Token Security
- Tokens are signed using HS256 algorithm
- Token expiration: 24 hours (configurable)
- Secret key is stored in application.properties (should use environment variables in production)

### CORS Configuration
- Currently allows all origins (development mode)
- Should be restricted to specific origins in production

### Session Management
- Stateless authentication (no server-side sessions)
- JWT tokens contain all necessary information

## Configuration

### JWT Settings (application.properties)
```properties
# JWT secret key (must be at least 256 bits for HS256)
app.jwt.secret=REDACTED

# JWT token expiration in milliseconds (24 hours)
app.jwt.expiration=86400000
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  INDEX idx_users_username (username),
  INDEX idx_users_email (email)
);
```

## Error Handling

### Common Error Responses

**400 Bad Request** - Invalid input
```json
{
  "error": "Username already exists"
}
```

**401 Unauthorized** - Authentication failed
```json
{
  "error": "Invalid username/email or password"
}
```

**403 Forbidden** - Insufficient permissions
```json
{
  "error": "Access denied"
}
```

**500 Internal Server Error** - Server error
```json
{
  "error": "Registration failed: <error message>"
}
```

## Testing Authentication

### Using cURL

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"testuser","password":"password123"}'
```

**Get Expenses (with token):**
```bash
curl -X GET http://localhost:8080/api/expenses \
  -H "Authorization: Bearer <your-jwt-token>"
```

## Production Recommendations

1. **Environment Variables**: Store JWT secret in environment variables, not in code
2. **HTTPS**: Use HTTPS in production to encrypt token transmission
3. **Token Refresh**: Implement refresh tokens for better security
4. **Rate Limiting**: Add rate limiting to prevent brute force attacks
5. **CORS**: Restrict CORS to specific origins
6. **Logging**: Implement security event logging
7. **Password Policy**: Enforce strong password requirements
8. **Account Lockout**: Implement account lockout after failed login attempts
9. **Email Verification**: Add email verification for new registrations
10. **Two-Factor Authentication**: Consider adding 2FA for enhanced security

## Next Steps

1. Update frontend login/register pages to use these endpoints
2. Add token to all expense API requests
3. Implement token expiration handling in frontend
4. Add user profile management features
5. Implement password reset functionality
6. Add role-based access control for admin features
