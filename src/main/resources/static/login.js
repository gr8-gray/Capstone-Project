/* 
 * ================================================================
 * SMART EXPENSE TRACKER – Login Page Script
 * Author: Eric Gray (Backend Developer)
 * Date Modified: November 2025
 * Modified By: Pukar Adhikari
 *
 * Description:
 * Manages login form submission, validation, and redirection to the
 * dashboard. Includes fixes to ensure the token is stored correctly
 * before transitioning pages.
 * ================================================================
 */


console.log("LOGIN.JS LOADED");

document.addEventListener("DOMContentLoaded", () => {
  console.log("DOM READY (login.html)");

  // Retrieve Login form from DOM
  const loginForm = document.getElementById("login-form");
  if (!loginForm) {
    console.error("ERROR: login-form NOT FOUND");
    return;
  }

  // Input Fields
  const usernameInput = document.getElementById("username");
  const passwordInput = document.getElementById("password");

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    console.log("SUBMIT EVENT FIRED");

    const usernameOrEmail = usernameInput.value.trim();
    const password = passwordInput.value;

    console.log("FINAL VALUES SENT TO BACKEND:", {
      usernameOrEmail,
      password: "*******", // Mask password for logs
    });

    if (!usernameOrEmail || !password) {
      alert("Please enter both username and password");
      return;
    }

    // Disable button to prevent double-clicks
    const submitBtn = loginForm.querySelector("button[type=submit]");
    submitBtn.disabled = true;
    submitBtn.textContent = "Logging in...";

    try {
      // Call backend
      const data = await login(usernameOrEmail, password);
      console.log("Login API data:", data);

      // Save token + user (handles token/jwt/accessToken etc.)
      saveAuthData(data);

      // Verify token actually stored
      const storedToken = getToken();
      console.log("Stored token after login:", storedToken);

      if (!storedToken || storedToken.length < 10) {
        console.error("Token not stored correctly.", { data, storedToken });
        alert(
          "Login succeeded on the server, but no valid token was received. Please contact your team."
        );
        submitBtn.disabled = false;
        submitBtn.textContent = "Login";
        return;
      }

      console.log("Token stored. Redirecting to dashboard...");
      window.location.href = "dashboard.html"; // Move to dashboard
    } catch (err) {
      console.error("Login error:", err);
      alert(err.message || "Login failed.");
      submitBtn.disabled = false;
      submitBtn.textContent = "Login";
    }
  });
});
