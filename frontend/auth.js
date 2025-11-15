// ================= AUTHENTICATION MODULE =================
// Author: Eric Gray - Backend Developer
// Description: Handles authentication, token management, and API calls

const AUTH_API_URL = "http://localhost:8080/api/auth";
const API_BASE_URL = "http://localhost:8080/api";

// ===== LOGGING UTILITY =====

const LOG_LEVELS = {
  ERROR: 'ERROR',
  WARN: 'WARN',
  INFO: 'INFO',
  DEBUG: 'DEBUG'
};

/**
 * Enhanced logging with timestamps
 */
function log(level, message, data = null) {
  const timestamp = new Date().toISOString();
  const prefix = `[${timestamp}] [${level}]`;
  
  if (data) {
    console[level.toLowerCase()](prefix, message, data);
  } else {
    console[level.toLowerCase()](prefix, message);
  }
}

/**
 * Display user-friendly error message
 */
function showError(message) {
  // Try to find error display element, or use alert as fallback
  const errorElement = document.getElementById('error-message');
  if (errorElement) {
    errorElement.textContent = message;
    errorElement.style.display = 'block';
    setTimeout(() => {
      errorElement.style.display = 'none';
    }, 5000);
  } else {
    alert(message);
  }
}

/**
 * Display success message
 */
function showSuccess(message) {
  const successElement = document.getElementById('success-message');
  if (successElement) {
    successElement.textContent = message;
    successElement.style.display = 'block';
    setTimeout(() => {
      successElement.style.display = 'none';
    }, 3000);
  }
}

// ===== TOKEN MANAGEMENT =====

/**
 * Store authentication token and user data
 */
function saveAuthData(authResponse) {
  localStorage.setItem('token', authResponse.token);
  localStorage.setItem('user', JSON.stringify({
    id: authResponse.userId,
    username: authResponse.username,
    email: authResponse.email,
    role: authResponse.role
  }));
}

/**
 * Get stored authentication token
 */
function getToken() {
  return localStorage.getItem('token');
}

/**
 * Get stored user data
 */
function getUser() {
  const userStr = localStorage.getItem('user');
  return userStr ? JSON.parse(userStr) : null;
}

/**
 * Check if user is authenticated
 */
function isAuthenticated() {
  return getToken() !== null;
}

/**
 * Clear authentication data
 */
function clearAuthData() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
}

/**
 * Logout user
 */
function logout() {
  clearAuthData();
  window.location.href = 'index.html';
}

// ===== API HELPER FUNCTIONS =====

/**
 * Make authenticated API request
 */
async function authenticatedFetch(url, options = {}) {
  const token = getToken();
  
  if (!token) {
    log(LOG_LEVELS.ERROR, 'Attempted authenticated request without token');
    throw new Error('Not authenticated');
  }

  log(LOG_LEVELS.DEBUG, `Making authenticated request to: ${url}`);

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
    ...options.headers
  };

  try {
    const response = await fetch(url, { ...options, headers });

    // Handle unauthorized response (token expired or invalid)
    if (response.status === 401) {
      log(LOG_LEVELS.WARN, 'Authentication expired, redirecting to login');
      clearAuthData();
      showError('Your session has expired. Please login again.');
      setTimeout(() => {
        window.location.href = 'index.html';
      }, 1500);
      throw new Error('Authentication expired. Please login again.');
    }

    // Handle server errors
    if (response.status >= 500) {
      log(LOG_LEVELS.ERROR, `Server error: ${response.status} ${response.statusText}`);
      throw new Error('Server error. Please try again later.');
    }

    log(LOG_LEVELS.DEBUG, `Request successful: ${response.status}`);
    return response;
  } catch (error) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      log(LOG_LEVELS.ERROR, 'Network error - backend may be down', error);
      throw new Error('Unable to connect to server. Please check if the backend is running.');
    }
    throw error;
  }
}

// ===== AUTHENTICATION API CALLS =====

/**
 * Register new user
 */
async function register(username, email, password) {
  log(LOG_LEVELS.INFO, `Attempting to register user: ${username}`);
  
  try {
    const response = await fetch(`${AUTH_API_URL}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password })
    });

    const data = await response.json();

    if (!response.ok) {
      const errorMsg = data.error || 'Registration failed';
      log(LOG_LEVELS.WARN, `Registration failed: ${errorMsg}`);
      throw new Error(errorMsg);
    }

    log(LOG_LEVELS.INFO, `User registered successfully: ${username}`);
    return data;
  } catch (error) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      log(LOG_LEVELS.ERROR, 'Network error during registration', error);
      throw new Error('Unable to connect to server. Please check if the backend is running.');
    }
    log(LOG_LEVELS.ERROR, 'Registration error:', error);
    throw error;
  }
}

/**
 * Login user
 */
async function login(usernameOrEmail, password) {
  log(LOG_LEVELS.INFO, `Attempting to login user: ${usernameOrEmail}`);
  
  try {
    const response = await fetch(`${AUTH_API_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usernameOrEmail, password })
    });

    const data = await response.json();

    if (!response.ok) {
      const errorMsg = data.error || 'Login failed';
      log(LOG_LEVELS.WARN, `Login failed for ${usernameOrEmail}: ${errorMsg}`);
      throw new Error(errorMsg);
    }

    log(LOG_LEVELS.INFO, `User logged in successfully: ${usernameOrEmail}`);
    return data;
  } catch (error) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      log(LOG_LEVELS.ERROR, 'Network error during login', error);
      throw new Error('Unable to connect to server. Please check if the backend is running.');
    }
    log(LOG_LEVELS.ERROR, 'Login error:', error);
    throw error;
  }
}

/**
 * Get current user profile
 */
async function getCurrentUserProfile() {
  log(LOG_LEVELS.DEBUG, 'Fetching current user profile');
  
  try {
    const response = await authenticatedFetch(`${AUTH_API_URL}/me`);
    const data = await response.json();
    log(LOG_LEVELS.DEBUG, 'User profile retrieved successfully');
    return data;
  } catch (error) {
    log(LOG_LEVELS.ERROR, 'Failed to get user profile:', error);
    throw error;
  }
}

/**
 * Check if username is available
 */
async function checkUsernameAvailability(username) {
  try {
    const response = await fetch(`${AUTH_API_URL}/check-username?username=${encodeURIComponent(username)}`);
    const data = await response.json();
    return !data.exists; // Return true if available (not exists)
  } catch (error) {
    console.error('Check username error:', error);
    return false;
  }
}

/**
 * Check if email is available
 */
async function checkEmailAvailability(email) {
  try {
    const response = await fetch(`${AUTH_API_URL}/check-email?email=${encodeURIComponent(email)}`);
    const data = await response.json();
    return !data.exists; // Return true if available (not exists)
  } catch (error) {
    console.error('Check email error:', error);
    return false;
  }
}

// ===== PAGE PROTECTION =====

/**
 * Protect page - redirect to login if not authenticated
 */
function requireAuth() {
  if (!isAuthenticated()) {
    window.location.href = 'index.html';
  }
}

/**
 * Redirect to dashboard if already authenticated
 */
function redirectIfAuthenticated() {
  if (isAuthenticated()) {
    window.location.href = 'dashboard.html';
  }
}

// ===== DISPLAY USER INFO =====

/**
 * Display user information in UI
 */
function displayUserInfo(elementId) {
  const user = getUser();
  const element = document.getElementById(elementId);
  
  if (user && element) {
    element.textContent = user.username;
  }
}
