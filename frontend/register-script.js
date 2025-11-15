// ================= REGISTRATION PAGE SCRIPT =================
// Author: Eric Gray - Backend Developer
// Description: Handles registration form submission and validation

document.addEventListener("DOMContentLoaded", () => {
  // Redirect to dashboard if already logged in
  redirectIfAuthenticated();

  const registerForm = document.getElementById("register-form");
  const nameInput = document.getElementById("name");
  const emailInput = document.getElementById("email");
  const passwordInput = document.getElementById("password");
  const confirmPasswordInput = document.getElementById("confirm-password");

  // Real-time username availability check
  let usernameCheckTimeout;
  nameInput.addEventListener("input", () => {
    clearTimeout(usernameCheckTimeout);
    const username = nameInput.value.trim();
    
    if (username.length >= 3) {
      usernameCheckTimeout = setTimeout(async () => {
        const isAvailable = await checkUsernameAvailability(username);
        if (!isAvailable) {
          nameInput.setCustomValidity("Username already taken");
        } else {
          nameInput.setCustomValidity("");
        }
      }, 500);
    }
  });

  // Real-time email availability check
  let emailCheckTimeout;
  emailInput.addEventListener("input", () => {
    clearTimeout(emailCheckTimeout);
    const email = emailInput.value.trim();
    
    if (email.includes("@")) {
      emailCheckTimeout = setTimeout(async () => {
        const isAvailable = await checkEmailAvailability(email);
        if (!isAvailable) {
          emailInput.setCustomValidity("Email already registered");
        } else {
          emailInput.setCustomValidity("");
        }
      }, 500);
    }
  });

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

    const username = nameInput.value.trim();
    const email = emailInput.value.trim();
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    // Validate inputs
    if (!username || !email || !password || !confirmPassword) {
      alert("Please fill in all fields");
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

    try {
      // Call register API
      const response = await register(username, email, password);
      
      // Save authentication data
      saveAuthData(response);

      // Show success message
      alert(`Welcome, ${response.username}! Your account has been created.`);

      // Redirect to dashboard
      window.location.href = "dashboard.html";
    } catch (error) {
      // Show error message
      alert(error.message || "Registration failed. Please try again.");
      
      // Reset button state
      submitButton.textContent = originalText;
      submitButton.disabled = false;
    }
  });
});
