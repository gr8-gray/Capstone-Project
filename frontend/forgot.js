/* 
 * ================================================================
 * SMART EXPENSE TRACKER – Login Page Script
 * Date Modified: November 2025
 * Author: Pukar Adhikari
 *
 * Description:
 * Manages reset password form submission, validation, and redirection to the
 * dashboard. Includes fixes to ensure the token is stored correctly
 * before transitioning pages.
 * ================================================================
 */
async function resetPassword() {
  const email = document.getElementById("email").value;

  const response = await fetch("http://localhost:8080/api/auth/reset-password", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email })
  });

  const data = await response.json();
  document.getElementById("response").innerText = data.message || data.error;
}
