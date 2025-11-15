// ================= LOGIN PAGE SCRIPT =================
// Author: Eric Gray - Backend Developer
// Description: Handles login form submission and authentication

document.addEventListener("DOMContentLoaded", () => {
  // Redirect to dashboard if already logged in
  redirectIfAuthenticated();

  const loginForm = document.getElementById("login-form");
  const emailInput = document.getElementById("email");
  const passwordInput = document.getElementById("password");

  // Handle login form submission
  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const usernameOrEmail = emailInput.value.trim();
    const password = passwordInput.value;

    // Validate inputs
    if (!usernameOrEmail || !password) {
      alert("Please enter both email/username and password");
      return;
    }

    // Show loading state
    const submitButton = loginForm.querySelector('button[type="submit"]');
    const originalText = submitButton.textContent;
    submitButton.textContent = "Logging in...";
    submitButton.disabled = true;

    try {
      // Call login API
      const response = await login(usernameOrEmail, password);
      
      // Save authentication data
      saveAuthData(response);

      // Show success message
      alert(`Welcome back, ${response.username}!`);

      // Redirect to dashboard
      window.location.href = "dashboard.html";
    } catch (error) {
      // Show error message
      alert(error.message || "Login failed. Please check your credentials.");
      
      // Reset button state
      submitButton.textContent = originalText;
      submitButton.disabled = false;
    }
  });
});
