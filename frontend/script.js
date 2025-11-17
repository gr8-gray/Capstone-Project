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


document.addEventListener("DOMContentLoaded", () => {

  console.log("[INFO] Waiting for token before initializing dashboard...");

  // ===== API BASE =====
  const API_BASE_URL = "http://localhost:8080/api/expenses";

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
    console.log("[INFO] Fetching expenses...");
    try {
      const res = await authenticatedFetch(API_BASE_URL);
      expenses = await res.json();
      renderTable();
    } catch (err) {
      console.error(err);
      alert("Could not load expenses from server.");
    }
  }

  async function addExpense(expense) {
    const res = await authenticatedFetch(API_BASE_URL, {
      method: "POST",
      body: JSON.stringify(expense),
    });
    return await res.json();
  }

  async function updateExpense(id, data) {
    const res = await authenticatedFetch(`${API_BASE_URL}/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
    return await res.json();
  }

  async function deleteExpense(id) {
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
          <button class="edit-btn" data-id="${exp.id}" data-index="${index}">✏️</button>
          <button class="delete-btn" data-id="${exp.id}" data-index="${index}">❌</button>
        </td>
      `;
      tbody.appendChild(row);
    });

    recalcTotal();
  }

  // ===== ADD EXPENSE =====
  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const expense = {
      description: nameEl.value.trim(),
      category: catEl.value,
      amount: Number(amtEl.value),
      date: dateEl.value,
    };

    if (!expense.description || !expense.category || !expense.date || expense.amount <= 0) {
      alert("Fill all fields correctly.");
      return;
    }

    try {
      await addExpense(expense);
      await fetchExpenses();
      form.reset();
    } catch {
      alert("Failed to add expense.");
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
        await fetchExpenses();
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
      ["Food", "Transport", "Utilities", "Entertainment", "Health", "Other"].forEach((c) => {
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
        await fetchExpenses();
      };
    }
  });

  // ===== WAIT FOR TOKEN, THEN AUTH + LOAD =====
  console.log("[INFO] Waiting for token before loading expenses...");

  let attempts = 0;
  const waitForToken = setInterval(() => {
    const token = localStorage.getItem("token");
    console.log("[DEBUG] Token check attempt", attempts, "value:", token);

    if (token && token.length > 10) {
      clearInterval(waitForToken);

      console.log("[INFO] Token detected — initializing dashboard.");

      // NOW SAFE TO RUN AUTH CHECK
      requireAuth();

      // NOW RENDER USER HEADER
      const user = getUser();
      const nav = document.querySelector("nav");
      if (nav && user) {
        const span = document.createElement("span");
        span.textContent = `Welcome, ${user.username}`;
        span.style.marginRight = "15px";
        span.style.fontWeight = "bold";
        span.style.color = "white";
        nav.prepend(span);
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

  function renderCharts() {
    const catTotals = {};
    const monthTotals = {};

    expenses.forEach((exp) => {
      const fixed = toLocalDate(exp.date);
      const month = new Date(fixed).toLocaleString("default", { month: "short" });

      catTotals[exp.category] = (catTotals[exp.category] || 0) + Number(exp.amount);
      monthTotals[month] = (monthTotals[month] || 0) + Number(exp.amount);
    });

    const categories = Object.keys(catTotals);
    const categoryAmounts = Object.values(catTotals);

    const months = Object.keys(monthTotals);
    const monthlyAmounts = Object.values(monthTotals);

    if (window.categoryChartObj) window.categoryChartObj.destroy();
    if (window.monthlyChartObj) window.monthlyChartObj.destroy();

    window.categoryChartObj = new Chart(document.getElementById("categoryChart"), {
      type: "pie",
      data: {
        labels: categories,
        datasets: [{ data: categoryAmounts }],
      },
    });

    window.monthlyChartObj = new Chart(document.getElementById("monthlyChart"), {
      type: "bar",
      data: {
        labels: months,
        datasets: [{ label: "Total", data: monthlyAmounts }],
      },
    });
  }

  // ===== LOGOUT =====
  document.getElementById("logoutBtn").addEventListener("click", () => {
    clearAuthData();
    window.location.href = "login.html";
  });
});
