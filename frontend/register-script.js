/* 
 * ================================================================
 * SMART EXPENSE TRACKER – Registration Page Script
 * Author: Eric Gray - Backend Developer
 * Date Modified: November 2025
 * Modified By: Pukar Adhikari
 *
 * Description:
 * Handles registration input validation, backend submission,
 * and automatic login after successful account creation.
 * Includes redirect logic to prevent logged-in users from viewing
 * the registration page.
 * ================================================================
 */

document.addEventListener("DOMContentLoaded", () => {
  redirectIfAuthenticated();   // If already logged in, go to dashboard

  // Retrieve all registration fields
  const registerForm = document.getElementById("register-form");
  const firstNameInput = document.getElementById("first-name");
  const lastNameInput = document.getElementById("last-name");
  const usernameInput = document.getElementById("username");
  const emailInput = document.getElementById("email");
  const passwordInput = document.getElementById("password");
  const confirmPasswordInput = document.getElementById("confirm-password");

  // Ensure form exists on the page
  if (!registerForm) {
    console.error("register-form not found in DOM");
    return;
  }

  // Registration Handler
  registerForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const firstName = firstNameInput.value.trim();
    const lastName = lastNameInput.value.trim();
    const username = usernameInput.value.trim();
    const email = emailInput.value.trim();
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    // Form Validation
    if (!firstName || !lastName || !username || !email || !password || !confirmPassword) {
      alert("Please fill in all fields.");
      return;
    }

    if (username.length < 3) {
      alert("Username must be at least 3 characters long.");
      return;
    }

    if (!email.includes("@")) {
      alert("Please enter a valid email.");
      return;
    }

    if (password.length < 6) {
      alert("Password must be at least 6 characters long.");
      return;
    }

    if (password !== confirmPassword) {
      alert("Passwords do not match.");
      return;
    }

    // UI Feedback
    const submitButton = registerForm.querySelector('button[type="submit"]');
    const originalText = submitButton.textContent;
    submitButton.textContent = "Creating account...";
    submitButton.disabled = true;

    try {
      // Calls register() in auth.js
      const authResponse = await register(firstName, lastName, username, email, password);

      console.log("Register response:", authResponse);

      // Save token + user info
      saveAuthData(authResponse);

      alert(`Welcome, ${authResponse.username || username}! Your account has been created.`);
      // Redirect to dashbaord
      window.location.href = "dashboard.html";
    } catch (err) {
      console.error("Registration error:", err);
      alert(err.message || "Registration failed. Please try again.");
    } 
    // Restore button state regardless of success/failure
    finally {
      submitButton.textContent = originalText;
      submitButton.disabled = false;
    }
  });
});
