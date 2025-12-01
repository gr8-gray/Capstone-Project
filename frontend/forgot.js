/*
 * ================================================================
 * SMART EXPENSE TRACKER – Forgot Password Script
 * Author: Pukar Adhikari
 * Date Modified: November 2025
 *
 * Handles:
 * 1. Sending email to get the secret question
 * 2. Displaying the secret question
 * 3. Letting user enter answer + new password
 * 4. Sending final reset request
 * ================================================================
 */

const API = "http://localhost:8080/api/auth";

// STEP 1 → Request secret question
async function requestSecretQuestion() {
    const email = document.getElementById("emailInput").value.trim();
    const msg = document.getElementById("messageBox");

    msg.textContent = "";
    msg.className = "message";

    if (!email) {
        msg.textContent = "Please enter your email.";
        msg.classList.add("error");
        return;
    }

    try {
        const res = await fetch(`${API}/forgot-password`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        });

        const data = await res.json();

        if (!res.ok) {
            msg.textContent = data.message || "Email not found.";
            msg.classList.add("error");
            return;
        }

        // SUCCESS → Show Step 2
        document.getElementById("step1").style.display = "none";
        document.getElementById("step2").style.display = "block";
        document.getElementById("questionBox").textContent = data.question;

        // Save email for step 2
        localStorage.setItem("resetEmail", email);

    } catch (err) {
        msg.textContent = "Server error.";
        msg.classList.add("error");
    }
}

// STEP 2 → Submit answer + new password
async function submitNewPassword() {
    const email = localStorage.getItem("resetEmail");
    const answer = document.getElementById("secretAnswerInput").value.trim();
    const newPassword = document.getElementById("newPassInput").value.trim();
    const msg2 = document.getElementById("messageBox2");

    msg2.textContent = "";
    msg2.className = "message";

    if (!answer || !newPassword) {
        msg2.textContent = "Please fill all fields.";
        msg2.classList.add("error");
        return;
    }

    try {
        const res = await fetch(`${API}/reset-password`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, answer, newPassword })
        });

        const data = await res.json();

        if (!res.ok) {
            msg2.textContent = data.message || "Incorrect answer.";
            msg2.classList.add("error");
            return;
        }

        msg2.textContent = "Password reset successfully!";
        msg2.classList.add("success");

        // Redirect to login after 2 sec
        setTimeout(() => {
            window.location.href = "login.html";
        }, 2000);

    } catch (err) {
        msg2.textContent = "Server error.";
        msg2.classList.add("error");
    }
}
