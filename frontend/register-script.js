// ================= REGISTRATION PAGE SCRIPT =================
// Author: Eric Gray - Backend Developer
// Description: Handles registration form submission and validation

document.addEventListener("DOMContentLoaded", () => {
  // Redirect to dashboard if already logged in
  redirectIfAuthenticated();

  const registerForm = document.getElementById("register-form");
  const nameInput = document.getElementById("full-name");
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
      alert("Usernawme must be at least 3 characters long");
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

    // Check username + email availability (only on submit)
    // try {
    //   const [isUsernameAvailable, isEmailAvailable] = await Promise.all([
    //     checkUsernameAvailability(username),
    //     checkEmailAvailability(email),
    //   ]);

    //   if (!isUsernameAvailable) {
    //     alert("Username already taken. Please choose another.");
    //     return;
    //   }

    //   if (!isEmailAvailable) {
    //     alert("Email already registered. Try logging in instead.");
    //     return;
    //   }
    // } catch (err) {
    //   console.error("Availability check failed:", err);
    //   // Optional: block registration if availability check fails
    //   // alert("Unable to verify username/email right now. Please try again.");
    //   // return;
    // }

    // Show loading state
    const submitButton = registerForm.querySelector('button[type="submit"]');
    const originalText = submitButton.textContent;
    submitButton.textContent = "Creating account...";
    submitButton.disabled = true;
    console.log("Submitting registration:" + originalText);

    try {
      // Call register API
      const response = await register(username, email, password);

      // Save authentication data (if backend returns token/user)
      saveAuthData(response);

      // Show success message
      showSuccess(
        `Welcome, ${
          response.username || username
        }! Your account has been created.`
      );

      // Redirect to dashboard
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
