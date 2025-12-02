/*
 * ================================================================
 * ================= AUTHENTICATION MODULE =================
 * Author: Eric Gray - Backend Developer
 * Description: Handles authentication, token management, and API calls
 *
 * Modified by Pukar Adhikari
 *
 * Description:
 * Handles login, registration, token management, and authenticated
 * API requests. Includes improved session handling and consistent
 * response parsing to support backend integration.
 * ================================================================
 */

const AUTH_API_URL = "http://localhost:8080/api/auth";
const API_BASE_URL = "http://localhost:8080/api";

// ===== LOGGING UTILITY =====
const LOG_LEVELS = {
  ERROR: "ERROR",
  WARN: "WARN",
  INFO: "INFO",
  DEBUG: "DEBUG",
};

function log(level, message, data = null) {
  const timestamp = new Date().toISOString();
  const prefix = `[${timestamp}] [${level}]`;
  if (data) {
    console[level.toLowerCase()](prefix, message, data);
  } else {
    console[level.toLowerCase()](prefix, message);
  }
}

// ===== MESSAGES =====
function showError(message) {
  const el = document.getElementById("error-message");
  if (el) {
    el.textContent = message;
    el.style.display = "block";
    setTimeout(() => (el.style.display = "none"), 5000);
  } else {
    alert(message);
  }
}

function showSuccess(message) {
  const el = document.getElementById("success-message");
  if (el) {
    el.textContent = message;
    el.style.display = "block";
    setTimeout(() => (el.style.display = "none"), 3000);
  }
}

// ===== TOKEN MANAGEMENT =====

// Store token + user info
function saveAuthData(authResponse) {
  
  const token =
    authResponse.token ||
    authResponse.jwt ||
    authResponse.accessToken ||
    authResponse.access_token;

  if (!token) {
    console.error("No token in auth response:", authResponse);
  }
  // Store token safely
  localStorage.setItem("token", token || "");

  // Store user info
  localStorage.setItem(
    "user",
    JSON.stringify({
      id: authResponse.id || authResponse.userId || null,
      username: authResponse.username || authResponse.userName || "",
      email: authResponse.email || "",
      role: authResponse.role || "USER",
    })
  );
}

// Retrieve stored JWT token
function getToken() {
  return localStorage.getItem("token");
}

// Retrieve stored user object
function getUser() {
  const s = localStorage.getItem("user");
  return s ? JSON.parse(s) : null;
}

// Check if user has a valid session
function isAuthenticated() {
  const t = getToken();
  return t !== null && t !== "";
}

// Clear all authentication data
function clearAuthData() {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
}

// Logout user and return to login screen
function logout() {
  clearAuthData();
  window.location.href = "login.html";
}

// ===== API HELPER =====
async function authenticatedFetch(url, options = {}) {
  const token = getToken();
  if (!token) {
    log(LOG_LEVELS.ERROR, "Attempted authenticated request without token");
    throw new Error("Not authenticated");
  }

  // Add required headers
  const headers = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
    ...options.headers,
  };

  try {
    const response = await fetch(url, { ...options, headers });

    // Token expired = redirect
    if (response.status === 401) {
      log(LOG_LEVELS.WARN, "401 from API, clearing auth and redirecting");
      clearAuthData();
      showError("Your session has expired. Please login again.");
      setTimeout(() => {
        window.location.href = "login.html";
      }, 1500);
      throw new Error("Authentication expired");
    }

    // Backend failure
    if (response.status >= 500) {
      log(LOG_LEVELS.ERROR, `Server error: ${response.status}`);
      throw new Error("Server error. Please try again later.");
    }

    return response;
  } catch (err) {
    if (err.name === "TypeError" && err.message.includes("fetch")) {
      log(LOG_LEVELS.ERROR, "Network error during API call", err);
      throw new Error(
        "Unable to connect to server. Please check if the backend is running."
      );
    }
    throw err;
  }
}

  // ===== AUTH API CALLS =====
  async function register(firstName, lastName, username, email, password) {
    log(LOG_LEVELS.INFO, `Attempting to register user: ${username}`);

    try {
      const response = await fetch(`${AUTH_API_URL}/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ firstName, lastName, username, email, password }),
      });

      // Safely parse JSON
      let data = await response.json().catch(() => ({}));

      // ----------- ERROR HANDLING -----------
      if (!response.ok) {
        let message = "Registration failed. Please try again.";

        // Use backend message EXACTLY as provided
        if (data && data.message) {
          message = data.message;
        }

        throw new Error(message);
      }
      // ----------- END ERROR HANDLING -----------

      log(LOG_LEVELS.INFO, "User registered successfully");
      return data;

    } catch (err) {
      // Hide technical errors from users
      if (err.name === "TypeError" && err.message.includes("fetch")) {
        console.error("Network/server error:", err);
        throw new Error("Something went wrong. Please try again.");
      }

      throw err;
    }
  }

async function login(usernameOrEmail, password) {
  console.log("AUTH.JS login() CALLED");

  try {
    const response = await fetch(`${AUTH_API_URL}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ usernameOrEmail, password }),
    });

    console.log("FETCH RESPONSE RAW:", response);

    const data = await response.json();
    console.log("FETCH JSON:", data);

    if (!response.ok) {
      throw new Error(data.error || "Login failed");
    }

    return data;
  } catch (err) {
    console.error("AUTH.JS LOGIN ERROR:", err);
    throw err;
  }
}

// Redirect if Authenticated()
function redirectIfAuthenticated() {
  const token = localStorage.getItem("token");
  if (!token) return;

  // Only redirect when on register or login pages
  const path = window.location.pathname;

  if (
    path.endsWith("login.html") ||
    path.endsWith("register.html") ||
    path.endsWith("/")
  ) {
    window.location.href = "dashboard.html";
  }
}

// On dashboard: if not logged in, kick back to login.
function requireAuth() {
  const page = window.location.pathname.split("/").pop();
  if (page === "dashboard.html" && !isAuthenticated()) {
    window.location.href = "login.html";
  }
}

// Show username in header if element exists
function displayUserInfo(elementId) {
  const user = getUser();
  const el = document.getElementById(elementId);
  if (user && el) {
    el.textContent = user.username;
  }
}
