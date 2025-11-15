# Frontend Authentication Integration - Testing Guide

## Overview
The frontend has been fully integrated with the JWT-based authentication system. All pages now require authentication, and API calls include JWT tokens.

## Files Created/Modified

### New Files
1. **auth.js** - Authentication module with token management and API helpers
2. **login.js** - Login page logic
3. **register-script.js** - Registration page logic

### Modified Files
1. **index.html** - Added authentication scripts
2. **register.html** - Added authentication scripts
3. **dashboard.html** - Added auth.js script
4. **script.js** - Updated to use authenticated API calls

## Testing Instructions

### 1. Start the Backend Server

```powershell
# Navigate to project root
cd "c:\Users\EricG\OneDrive\Desktop\CAPSTONE PROJ\api endpoint addition\SmartExpenseTrackingApp"

# Start the Spring Boot application
.\mvnw.cmd spring-boot:run
```

Wait for the message: `Started ExpenseApiApplication in X.XXX seconds`

### 2. Open the Frontend

Open `frontend/index.html` in your web browser (or use a local server).

### 3. Test Registration Flow

1. Click "Register here" link on login page
2. Fill in the registration form:
   - **Username**: testuser (minimum 3 characters)
   - **Email**: test@example.com
   - **Password**: password123 (minimum 6 characters)
   - **Confirm Password**: password123
3. Click "Register" button
4. You should see a success message and be redirected to the dashboard
5. The dashboard should display "Welcome, testuser" in the navigation

### 4. Test Login Flow

1. Logout from the dashboard
2. On the login page, enter:
   - **Email or Username**: testuser (or test@example.com)
   - **Password**: password123
3. Click "Login" button
4. You should see a welcome message and be redirected to the dashboard

### 5. Test Dashboard with Authentication

1. After logging in, you should be on the dashboard
2. Try adding an expense:
   - Description: Groceries
   - Category: Food
   - Amount: 50.00
   - Date: Select today's date
3. Click "Add Expense"
4. The expense should be saved and displayed in the table
5. All API calls now include the JWT token automatically

### 6. Test Logout

1. Click the "Logout" button in the navigation
2. Confirm the logout
3. You should be redirected to the login page
4. Try accessing the dashboard directly by going to `dashboard.html`
5. You should be automatically redirected back to the login page

### 7. Test Token Expiration

1. Login to the application
2. Open browser Developer Tools (F12)
3. Go to Application/Storage > Local Storage
4. Find and delete the `token` entry
5. Try to add an expense or refresh the page
6. You should be automatically redirected to the login page

### 8. Test Validation

**Registration Validation:**
- Try registering with username < 3 characters → Should show error
- Try registering with password < 6 characters → Should show error
- Try registering with mismatched passwords → Should show error
- Try registering with an existing username → Should show error
- Try registering with an existing email → Should show error

**Login Validation:**
- Try logging in with wrong credentials → Should show "Invalid username/email or password"
- Try logging in with empty fields → Should require fields to be filled

## Browser Developer Tools

### Check Token Storage

1. Open Developer Tools (F12)
2. Go to Application/Storage > Local Storage
3. You should see:
   - `token`: JWT token string
   - `user`: JSON object with user info

### Monitor API Calls

1. Open Developer Tools (F12)
2. Go to Network tab
3. Filter by XHR/Fetch
4. Watch API calls and verify:
   - Login/Register calls to `/api/auth/login` and `/api/auth/register`
   - Expense API calls include `Authorization: Bearer <token>` header
   - Protected endpoints return 401 if token is missing/invalid

### Check Console for Errors

1. Open Developer Tools (F12)
2. Go to Console tab
3. Look for any error messages
4. Authentication errors should be logged with descriptive messages

## API Endpoints Being Called

### Authentication Endpoints (Public)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `GET /api/auth/check-username?username=X` - Check username availability
- `GET /api/auth/check-email?email=X` - Check email availability

### Protected Endpoints (Require Token)
- `GET /api/expenses` - Fetch all expenses
- `POST /api/expenses` - Create expense
- `PUT /api/expenses/{id}` - Update expense
- `DELETE /api/expenses/{id}` - Delete expense
- `GET /api/auth/me` - Get current user profile

## Expected Behavior

### ✅ Correct Behavior

1. **First Visit**: Redirected to login page
2. **After Login**: JWT token stored in localStorage, redirected to dashboard
3. **Dashboard Access**: Shows "Welcome, [username]" message
4. **Expense Operations**: All CRUD operations work with authentication
5. **Logout**: Clears token, redirects to login
6. **Direct Dashboard Access**: Redirects to login if not authenticated
7. **Token Expiry**: Redirects to login when token expires (24 hours)

### ❌ Issues to Watch For

1. **CORS Errors**: Make sure backend is running and CORS is enabled
2. **401 Unauthorized**: Check if token is being sent in Authorization header
3. **Token Not Stored**: Check browser localStorage in dev tools
4. **Redirect Loop**: Make sure auth check logic is correct
5. **Backend Connection**: Ensure backend is running on port 8080

## Testing with cURL

You can also test the API directly with cURL:

```powershell
# Register
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"curluser\",\"email\":\"curl@example.com\",\"password\":\"password123\"}'

# Login
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"usernameOrEmail\":\"curluser\",\"password\":\"password123\"}'

# Get expenses (replace TOKEN with actual token from login response)
curl -X GET http://localhost:8080/api/expenses `
  -H "Authorization: Bearer TOKEN"
```

## Troubleshooting

### Issue: Login fails with CORS error
**Solution**: Ensure backend server is running and CORS is configured properly

### Issue: Token not being sent with requests
**Solution**: Check that `auth.js` is loaded before `script.js` in dashboard.html

### Issue: Cannot register - username/email already exists
**Solution**: Database is in-memory (H2), restart the backend to clear data

### Issue: Redirected to login after successful login
**Solution**: Check browser console for errors, verify token is being stored

### Issue: 401 Unauthorized on all requests
**Solution**: Verify token is valid and not expired, check Authorization header format

## Next Steps

After successful testing:

1. **Production Deployment**:
   - Change JWT secret to environment variable
   - Enable HTTPS
   - Configure proper CORS origins
   - Switch from H2 to MySQL database

2. **Additional Features**:
   - Add "Remember Me" functionality
   - Implement password reset via email
   - Add email verification for new accounts
   - Implement token refresh mechanism
   - Add role-based access control

3. **UI/UX Improvements**:
   - Add loading spinners during API calls
   - Improve error messages display
   - Add password strength indicator
   - Show token expiration warning
   - Add profile management page

## Success Criteria

✅ Users can register new accounts  
✅ Users can login with username or email  
✅ Dashboard is protected and requires authentication  
✅ All expense operations work with JWT token  
✅ Logout clears token and redirects to login  
✅ Invalid/expired tokens redirect to login  
✅ User information is displayed in dashboard  
✅ No console errors during normal operation  

## Support

If you encounter any issues:
1. Check browser console for JavaScript errors
2. Check network tab for failed API calls
3. Verify backend server is running
4. Check backend console logs for errors
5. Ensure all new files are in the frontend directory
