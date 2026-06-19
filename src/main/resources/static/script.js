/* 
 * ================================================================
 * SMART EXPENSE TRACKER – Dashboard Logic
 * Author: Pukar Adhikari (Frontend Developer / Tester)
 * Date Modified: November 2025
 * Connected to the Backend API by Eric Gray
 *
 * Description:
 * Controls all dashboard behavior: fetching expenses, updating UI,
 * calculating totals, editing entries, deleting records, rendering
 * charts, and displaying user info. Includes token-waiting logic
 * to prevent double-login issues.
 * ================================================================
 */

(function securePageLoad() {
  const token = localStorage.getItem("token");

  // If no token, redirect before anything renders
  if (!token || token.length < 10) {
    window.location.replace("login.html");
  }
})();

document.addEventListener("DOMContentLoaded", () => {
  
  // Prevent ANY accidental form submission refresh
  document.getElementById("expense-form").addEventListener("submit", (e) => {
    e.preventDefault();
    return false;
  });

  // ===== API BASE =====
  const API_BASE_URL = "/api/expenses";

  // ===== DATE FIXERS =====
  function toLocalDate(dateStr) {
    const [y, m, d] = dateStr.split("-").map(Number);
    return new Date(y, m - 1, d).toISOString().split("T")[0];
  }

  function toLocale(dateStr) {
    const fixed = toLocalDate(dateStr);
    const [y, m, d] = fixed.split("-");
    return `${m}/${d}/${y}`;
  }
  
  // ===== WAIT UNTIL TOKEN EXISTS (PREVENT 403 + DELAYS) =====
  async function waitForValidToken() {
    return new Promise(resolve => {
      const check = setInterval(() => {
        const t = localStorage.getItem("token");
        if (t && t.length > 10) {
          clearInterval(check);
          resolve(t);
        }
      }, 50);
    });
  }

  // ===== DOM ELEMENTS =====
  const form = document.getElementById("expense-form");
  const nameEl = document.getElementById("expense-name");
  const catEl = document.getElementById("expense-category");
  const amtEl = document.getElementById("expense-amount");
  const dateEl = document.getElementById("expense-date");
  const tbody = document.getElementById("table-body");
  const totalCell = document.getElementById("total-cell");

  let expenses = [];

  // ===== API CALLS =====
  async function fetchExpenses() {
    await waitForValidToken();
    try {
      const res = await authenticatedFetch(API_BASE_URL);
      expenses = await res.json();
      renderTable();
      populateMonthFilter();
    } catch (err) {
      console.error(err);
      alert("Could not load expenses from server.");
    }
  }

  async function addExpense(expense) {
    await waitForValidToken();
    const res = await authenticatedFetch(API_BASE_URL, {
      method: "POST",
      body: JSON.stringify(expense),
    });
    return await res.json();
  }

  async function updateExpense(id, data) {
    await waitForValidToken();
    const res = await authenticatedFetch(`${API_BASE_URL}/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
    return await res.json();
  }

  async function deleteExpense(id) {
    await waitForValidToken();
    await authenticatedFetch(`${API_BASE_URL}/${id}`, { method: "DELETE" });
  }

  // ===== TOTAL =====
  function recalcTotal() {
    let sum = expenses.reduce((s, e) => s + Number(e.amount), 0);
    totalCell.innerHTML = `<strong>$${sum.toFixed(2)}</strong>`;
  }

  // ===== RENDER TABLE =====
  function renderTable() {
    tbody.innerHTML = "";

    expenses.forEach((exp, index) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${exp.description}</td>
        <td>${exp.category}</td>
        <td>$${Number(exp.amount).toFixed(2)}</td>
        <td>${toLocale(exp.date)}</td>
        <td>
          <button class="edit-btn" type="button" data-id="${exp.id}" data-index="${index}">✏️</button>
          <button class="delete-btn" type="button" data-id="${exp.id}" data-index="${index}">❌</button>
        </td>
      `;
      tbody.appendChild(row);
    });

    recalcTotal();
  }

  // ===== ADD EXPENSE =====
  document.getElementById("addBtn").addEventListener("click", async () => {
    const expense = {
      description: nameEl.value.trim(),
      category: catEl.value,
      amount: Number(amtEl.value),
      date: dateEl.value
    };

    if (!expense.description || !expense.category || !expense.date || expense.amount <= 0) {
      alert("Fill all fields correctly.");
      return;
    }

    try {
      const res = await addExpense(expense);

      if (res && res.id) {
      // Update client immediately
        expenses.push(res);
        renderTable();
        form.reset();
    } else {
      alert("Unexpected server response.");
    }

  } catch (err) {
    console.error("Add expense error:", err);
    alert("Could not add expense. Please try again.");
  }
});

  // ===== EDIT / DELETE =====
  tbody.addEventListener("click", async (e) => {
    const btn = e.target;
    const id = btn.dataset.id;
    const idx = btn.dataset.index;

    // DELETE
    if (btn.classList.contains("delete-btn")) {
      if (confirm("Delete this expense?")) {
        await deleteExpense(id);
        expenses.splice(idx, 1);
        renderTable();
      }
      return;
    }

    // EDIT
    if (btn.classList.contains("edit-btn")) {
      const row = btn.closest("tr");
      const exp = expenses[idx];

      const nameInput = document.createElement("input");
      nameInput.value = exp.description;

      const categorySelect = document.createElement("select");
      ["Food & Dining", "Transportation", "Shopping", "Entertainment", "Bills & Utilities", "Healthcare", "Education", "Travel", "Home & Garden", "Personal Care", "Insurance", "Investments", "Gifts & Donations", "Business", "Taxes", "Other"].forEach((c) => {
        const opt = document.createElement("option");
        opt.value = opt.textContent = c;
        if (c === exp.category) opt.selected = true;
        categorySelect.appendChild(opt);
      });

      const amountInput = document.createElement("input");
      amountInput.type = "number";
      amountInput.step = "0.01";
      amountInput.value = exp.amount;

      const dateInput = document.createElement("input");
      dateInput.type = "date";
      dateInput.value = toLocalDate(exp.date);

      row.children[0].innerHTML = "";
      row.children[0].appendChild(nameInput);
      row.children[1].innerHTML = "";
      row.children[1].appendChild(categorySelect);
      row.children[2].innerHTML = "";
      row.children[2].appendChild(amountInput);
      row.children[3].innerHTML = "";
      row.children[3].appendChild(dateInput);

      btn.textContent = "💾";
      btn.style.background = "#4CAF50";
      btn.style.color = "white";

      btn.onclick = async () => {
        const updated = {
          description: nameInput.value.trim(),
          category: categorySelect.value,
          amount: parseFloat(amountInput.value),
          date: dateInput.value,
        };

        if (!updated.description || !updated.category || updated.amount <= 0 || !updated.date) {
          alert("Enter valid values.");
          return;
        }

        await updateExpense(id, updated);
        expenses[idx] = { ...expenses[idx], ...updated };
        renderTable();
      };
    }
  });

  // ===== WAIT FOR TOKEN, THEN AUTH + LOAD =====
  let attempts = 0;
  const waitForToken = setInterval(() => {
    const token = localStorage.getItem("token");

    if (token && token.length > 10) {
      clearInterval(waitForToken);

      // NOW SAFE TO RUN AUTH CHECK
      requireAuth();

      // NOW RENDER USER HEADER
      const user = getUser();
      if (user) {
        const welcomeEl = document.getElementById("welcomeUser");
        if (welcomeEl.textContent.trim() === "") {
          welcomeEl.textContent = "Welcome, " + user.username;
        }
      }

      // NOW LOAD EXPENSES
      fetchExpenses();
    }

    attempts++;
    if (attempts > 30) {  // 3 seconds max
      clearInterval(waitForToken);
      alert("Could not load expenses from server.");
    }
  }, 100);

  // ===== CHARTS =====
  const viewBtn = document.getElementById("viewReportBtn");
  const chartSection = document.getElementById("chartSection");
  let chartsVisible = false;

  viewBtn.addEventListener("click", () => {
    chartsVisible = !chartsVisible;
    chartSection.style.display = chartsVisible ? "block" : "none";
    viewBtn.textContent = chartsVisible ? "📉 Hide Reports" : "📊 View Reports";
    if (chartsVisible) renderCharts();
  });

  // ===== POPULATE MONTH FILTER =====
  function populateMonthFilter() {
    const monthFilter = document.getElementById("monthFilter");
    if (!monthFilter) return;

    // Always show full list of months
    const allMonths = [
      "Jan","Feb","Mar","Apr","May","Jun",
      "Jul","Aug","Sep","Oct","Nov","Dec"
    ];

    monthFilter.innerHTML = `<option value="all">All Months</option>`;

    allMonths.forEach(m => {
      monthFilter.innerHTML += `<option value="${m}">${m}</option>`;
    });

    // When user changes month -> update charts instantly
    monthFilter.addEventListener("change", renderCharts);
  }


  function renderCharts() {
    const monthFilter = document.getElementById("monthFilter");
    const selectedMonth = monthFilter ? monthFilter.value : "all";

    // Always track totals for all 12 months
    const monthTotals = {
      Jan: 0, Feb: 0, Mar: 0, Apr: 0, May: 0, Jun: 0,
      Jul: 0, Aug: 0, Sep: 0, Oct: 0, Nov: 0, Dec: 0,
    };

    // Category totals (pie chart)
    const categoryTotals = {};

    expenses.forEach(exp => {
      const fixed = toLocalDate(exp.date);
      const date = new Date(fixed);

      const monthName = date.toLocaleString("default", { month: "short" });

      // Always add to month totals (bar chart)
      monthTotals[monthName] += Number(exp.amount);

      // For pie chart — filter by selected month
      if (selectedMonth === "all" || selectedMonth === monthName) {
        categoryTotals[exp.category] =
          (categoryTotals[exp.category] || 0) + Number(exp.amount);
      }
    });

    // Prepare pie data
    const categoryLabels = Object.keys(categoryTotals);
    const categoryAmounts = Object.values(categoryTotals);

    // Prepare bar data (fixed order)
    const monthsOrdered = Object.keys(monthTotals);
    const monthAmounts = Object.values(monthTotals);

    // Destroy old charts
    if (window.categoryChartObj) window.categoryChartObj.destroy();
    if (window.monthlyChartObj) window.monthlyChartObj.destroy();

    // ===== ON-BRAND CHART PALETTE (visual only) =====
    const PIE_PALETTE = [
      "#2ee6a6", "#5ea0ff", "#ffc24b", "#9b8cff",
      "#ff5d73", "#3ad1c2", "#f78fb3", "#ffd66b"
    ];

    // ===== CATEGORY PIE CHART =====
    window.categoryChartObj = new Chart(document.getElementById("categoryChart"), {
      type: "doughnut",
      data: {
        labels: categoryLabels,
        datasets: [{
          data: categoryAmounts,
          backgroundColor: categoryLabels.map((_, i) => PIE_PALETTE[i % PIE_PALETTE.length]),
          borderColor: "rgba(10,14,26,0.9)",
          borderWidth: 3,
          hoverOffset: 6
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: "62%",
        plugins: { legend: { position: "bottom" } }
      }
    });

    // ===== MONTHLY BAR GRAPH =====
    const barCtx = document.getElementById("monthlyChart").getContext("2d");
    const barGrad = barCtx.createLinearGradient(0, 0, 0, 300);
    barGrad.addColorStop(0, "rgba(46,230,166,0.95)");
    barGrad.addColorStop(1, "rgba(18,184,134,0.35)");

    window.monthlyChartObj = new Chart(document.getElementById("monthlyChart"), {
      type: "bar",
      data: {
        labels: monthsOrdered,
        datasets: [{
          label: "Monthly Total ($)",
          data: monthAmounts,
          backgroundColor: barGrad,
          borderColor: "#2ee6a6",
          borderWidth: 1,
          borderRadius: 8,
          maxBarThickness: 34
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          x: { grid: { display: false } },
          y: { grid: { color: "rgba(255,255,255,0.06)" }, beginAtZero: true }
        }
      }
    });
  }

  // ===== Change Password =====
  document.getElementById("changePasswordLink").addEventListener("click", () => {
    window.location.href = "change_password.html";
  });

  // ===== LOGOUT =====
  document.getElementById("logoutBtn").addEventListener("click", () => {
    clearAuthData();
    window.location.href = "login.html";
  });
});
