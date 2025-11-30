// ================= REGISTRATION PAGE SCRIPT =================
// Author: Eric Gray - Backend Developer
// Description: Handles registration form submission and validation

document.addEventListener("DOMContentLoaded", () => {
  // Redirect to dashboard if already logged in
  redirectIfAuthenticated();

  const registerForm = document.getElementById("register-form");
  const firstNameInput = document.getElementById("first-name");
  const lastNameInput = document.getElementById("last-name");
  const usernameInput = document.getElementById("username");
  const emailInput = document.getElementById("email");
  const passwordInput = document.getElementById("password");
  const confirmPasswordInput = document.getElementById("confirm-password");

  // Real-time username availability check
  // (Disabled for now: we only check on submit to avoid weird behavior)
  // let usernameCheckTimeout;
  // nameInput.addEventListener("input", () => {
  //   clearTimeout(usernameCheckTimeout);
  //   const username = nameInput.value.trim();
  //   // You can re-enable a debounced live check here later if you want
  // });

  // Password confirmation validation
  confirmPasswordInput.addEventListener("input", () => {
    if (passwordInput.value !== confirmPasswordInput.value) {
      confirmPasswordInput.setCustomValidity("Passwords do not match");
    } else {
      confirmPasswordInput.setCustomValidity("");
    }
  });

  // Handle registration form submission
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
      alert("Username must be at least 3 characters long");
      return;
    }

    if (password.length < 6) {
      alert("Password must be at least 6 characters long");
      return;
    }

    if (password !== confirmPassword) {
      alert("Passwords do not match");
      return;
    }

    // Show loading state
    const submitButton = registerForm.querySelector('button[type="submit"]');
    const originalText = submitButton.textContent;
    submitButton.textContent = "Creating account...";
    submitButton.disabled = true;
    console.log("Submitting registration:" + originalText);

    try {
      // Calls register() in auth.js
      const authResponse = await register(firstName, lastName, username, email, password);

      console.log("Register response:", authResponse);

      // Save token + user info
      saveAuthData(authResponse);

      alert(`Welcome, ${authResponse.username || username}! Your account has been created.`);
      // Redirect to dashbaord
      window.location.href = "dashboard.html";
    } catch (error) {
      console.error("Registration error:", error);

      // Use the real message from backend (e.g. "User already exists with username: dmitc072")
      showError(error.message || "Registration failed. Please try again.");
    } finally {
      // Reset button state
      submitButton.textContent = originalText;
      submitButton.disabled = false;
    }
  });
});
