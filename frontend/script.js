// ================= SMART EXPENSE TRACKER =================
// Author: Pukar Adhikari (Frontend Developer / Tester)
// Modified: Connected to Backend API by Eric Gray
// Description: Handles all dashboard functionality including
// adding, editing, deleting expenses, rendering totals, charts,
// and managing logout behavior.

document.addEventListener("DOMContentLoaded", () => {

  // ===== API CONFIGURATION =====
  const API_BASE_URL = "http://localhost:8080/api/expenses";

  // ===== DOM ELEMENT REFERENCES =====
  const form = document.getElementById("expense-form");         // Add Expense form
  const nameEl = document.getElementById("expense-name");       // Expense name input
  const catEl = document.getElementById("expense-category");    // Category dropdown
  const amtEl = document.getElementById("expense-amount");      // Amount input
  const dateEl = document.getElementById("expense-date");       // Date input
  const tbody = document.getElementById("table-body");          // Table body for listing expenses
  const totalCell = document.getElementById("total-cell");      // Total amount cell

  // Store expenses in memory (loaded from backend)
  let expenses = [];

  // Helper functions for formatting currency and date
  const fmtMoney = (n) => `$${Number(n).toFixed(2)}`;
  const toLocale = (iso) => new Date(iso).toLocaleDateString();

  // ===== API HELPER FUNCTIONS =====
  
  // Fetch all expenses from backend
  async function fetchExpenses() {
    try {
      const response = await fetch(API_BASE_URL);
      if (!response.ok) throw new Error("Failed to fetch expenses");
      expenses = await response.json();
      renderTable();
    } catch (error) {
      console.error("Error fetching expenses:", error);
      alert("Failed to load expenses from server. Please check if the backend is running.");
    }
  }

  // Add new expense to backend
  async function addExpense(expenseData) {
    try {
      const response = await fetch(API_BASE_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(expenseData)
      });
      if (!response.ok) throw new Error("Failed to create expense");
      return await response.json();
    } catch (error) {
      console.error("Error adding expense:", error);
      throw error;
    }
  }

  // Update expense on backend
  async function updateExpense(id, expenseData) {
    try {
      const response = await fetch(`${API_BASE_URL}/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(expenseData)
      });
      if (!response.ok) throw new Error("Failed to update expense");
      return await response.json();
    } catch (error) {
      console.error("Error updating expense:", error);
      throw error;
    }
  }

  // Delete expense from backend
  async function deleteExpense(id) {
    try {
      const response = await fetch(`${API_BASE_URL}/${id}`, {
        method: "DELETE"
      });
      if (!response.ok) throw new Error("Failed to delete expense");
    } catch (error) {
      console.error("Error deleting expense:", error);
      throw error;
    }
  }

  // ===== RECALCULATE AND DISPLAY TOTAL =====
  function recalcTotal() {
    const sum = expenses.reduce((acc, e) => acc + Number(e.amount), 0);
    totalCell.innerHTML = `<strong>${fmtMoney(sum)}</strong>`;
  }

  // ===== RENDER TABLE CONTENT =====
  function renderTable() {
    tbody.innerHTML = "";
    expenses.forEach((exp, index) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${exp.description}</td>
        <td>${exp.category}</td>
        <td>${fmtMoney(exp.amount)}</td>
        <td>${toLocale(exp.date)}</td>
        <td>
          <button class="edit-btn" data-id="${exp.id}" data-index="${index}">✏️</button>
          <button class="delete-btn" data-id="${exp.id}" data-index="${index}">❌</button>
        </td>
      `;
      tbody.appendChild(row);
    });
    recalcTotal(); // Update total after rendering
  }

  // ===== ADD NEW EXPENSE =====
  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    // Get form values
    const description = nameEl.value.trim();
    const category = catEl.value;
    const amount = Number(amtEl.value);
    const date = dateEl.value;

    // Validate all fields
    if (!description || !category || !date || amount <= 0) {
      alert("Please fill out all fields correctly.");
      return;
    }

    // Create expense object matching backend model
    const expenseData = {
      description,
      category,
      amount,
      date
    };

    try {
      // Add new expense via API
      await addExpense(expenseData);
      await fetchExpenses(); // Reload expenses from backend
      form.reset(); // Clear form inputs
      alert("Expense added successfully!");
    } catch (error) {
      alert("Failed to add expense. Please try again.");
    }
  });

  // ===== HANDLE EDIT AND DELETE BUTTONS =====
  tbody.addEventListener("click", async (e) => {
    const target = e.target;
    const index = target.dataset.index;
    const expenseId = target.dataset.id;

    // --- DELETE EXPENSE ---
    if (target.classList.contains("delete-btn")) {
      if (confirm("Delete this expense?")) {
        try {
          await deleteExpense(expenseId);
          await fetchExpenses(); // Reload expenses from backend
          alert("Expense deleted successfully!");
        } catch (error) {
          alert("Failed to delete expense. Please try again.");
        }
      }
    }

    // --- EDIT EXPENSE ---
    if (target.classList.contains("edit-btn")) {
      const row = target.closest("tr");
      const exp = expenses[index];

      // Create editable input fields
      const nameInput = document.createElement("input");
      nameInput.type = "text";
      nameInput.value = exp.description;

      // Dropdown for category options
      const categorySelect = document.createElement("select");
      const categories = ["Food", "Transport", "Utilities", "Entertainment", "Health", "Other"];
      categories.forEach((cat) => {
        const option = document.createElement("option");
        option.value = cat;
        option.textContent = cat;
        if (cat === exp.category) option.selected = true;
        categorySelect.appendChild(option);
      });

      const amountInput = document.createElement("input");
      amountInput.type = "number";
      amountInput.step = "0.01";
      amountInput.min = "0";
      amountInput.value = exp.amount;

      const dateInput = document.createElement("input");
      dateInput.type = "date";
      dateInput.value = exp.date;

      // Replace table cells with editable inputs
      row.children[0].innerHTML = "";
      row.children[0].appendChild(nameInput);
      row.children[1].innerHTML = "";
      row.children[1].appendChild(categorySelect);
      row.children[2].innerHTML = "";
      row.children[2].appendChild(amountInput);
      row.children[3].innerHTML = "";
      row.children[3].appendChild(dateInput);

      // Change edit button to a save button (💾)
      target.textContent = "💾";
      target.style.background = "#4CAF50";
      target.style.color = "white";

      // Save updated expense when clicked again
      target.onclick = async () => {
        const updatedExpense = {
          description: nameInput.value.trim(),
          category: categorySelect.value,
          amount: parseFloat(amountInput.value),
          date: dateInput.value,
        };

        // Validate before saving
        if (
          !updatedExpense.description ||
          !updatedExpense.category ||
          isNaN(updatedExpense.amount) ||
          updatedExpense.amount <= 0 ||
          !updatedExpense.date
        ) {
          alert("Please enter valid values before saving.");
          return;
        }

        try {
          // Update expense via API
          await updateExpense(expenseId, updatedExpense);
          await fetchExpenses(); // Reload expenses from backend
          alert("Expense updated successfully!");
        } catch (error) {
          alert("Failed to update expense. Please try again.");
        }
      };
    }
  });

  // ===== INITIALIZE TABLE ON PAGE LOAD =====
  fetchExpenses(); // Load expenses from backend on page load

  // ===== CHARTS SECTION =====
  const viewBtn = document.getElementById("viewReportBtn");   // Button to toggle charts
  const chartSection = document.getElementById("chartSection"); // Chart container
  let chartsVisible = false;

  // Toggle chart section visibility
  viewBtn.addEventListener("click", () => {
    chartsVisible = !chartsVisible;
    chartSection.style.display = chartsVisible ? "block" : "none";
    viewBtn.textContent = chartsVisible ? "📉 Hide Reports" : "📊 View Reports";

    if (chartsVisible) renderCharts(); // Render charts when section is shown

    // Handle month filter changes
    const monthFilter = document.getElementById("monthFilter");
    monthFilter.addEventListener("change", () => {
      const selectedMonth = monthFilter.value;
      renderCharts(selectedMonth);
    });
  });

  // ===== RENDER CHARTS FUNCTION =====
  function renderCharts(selectedMonth = "all") {
    // Use expenses from backend (already loaded in memory)
    const savedExpenses = expenses;

    // --- Filter expenses by selected month ---
    const filteredExpenses = savedExpenses.filter((exp) => {
      if (selectedMonth === "all") return true;
      const expMonth = new Date(exp.date).toLocaleString("default", { month: "short" });
      return expMonth === selectedMonth;
    });

    // --- Aggregate by category for Pie Chart ---
    const categoryTotals = {};
    filteredExpenses.forEach((exp) => {
      categoryTotals[exp.category] = (categoryTotals[exp.category] || 0) + Number(exp.amount);
    });

    const categories = Object.keys(categoryTotals);
    const categoryAmounts = Object.values(categoryTotals);

    // --- Aggregate by month for Bar Chart ---
    const monthlyTotals = {};
    savedExpenses.forEach((exp) => {
      const month = new Date(exp.date).toLocaleString("default", { month: "short" });
      monthlyTotals[month] = (monthlyTotals[month] || 0) + Number(exp.amount);
    });

    const months = Object.keys(monthlyTotals);
    const monthlyAmounts = Object.values(monthlyTotals);

    // Destroy previous charts (if they exist) to avoid overlapping
    if (window.categoryChartObj) window.categoryChartObj.destroy();
    if (window.monthlyChartObj) window.monthlyChartObj.destroy();

    // --- PIE CHART: Category Breakdown ---
    const ctx1 = document.getElementById("categoryChart").getContext("2d");
    window.categoryChartObj = new Chart(ctx1, {
      type: "pie",
      data: {
        labels: categories.length ? categories : ["No Data"],
        datasets: [
          {
            data: categoryAmounts.length ? categoryAmounts : [1],
            backgroundColor: ["#007acc", "#00bcd4", "#ffcc00", "#ff7043", "#4caf50", "#9c27b0"],
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          title: {
            display: true,
            text:
              selectedMonth === "all"
                ? "All Expenses by Category"
                : `Expenses in ${selectedMonth} by Category`,
          },
          legend: { position: "bottom" },
        },
      },
    });

    // --- BAR CHART: Monthly Trend ---
    const ctx2 = document.getElementById("monthlyChart").getContext("2d");
    window.monthlyChartObj = new Chart(ctx2, {
      type: "bar",
      data: {
        labels: months,
        datasets: [
          {
            label: "Spending ($)",
            data: monthlyAmounts,
            backgroundColor: "#007acc",
          },
        ],
      },
      options: {
        responsive: true,
        scales: { y: { beginAtZero: true } },
        plugins: { title: { display: true, text: "Monthly Spending Trend" } },
      },
    });
  }

  // ===== LOGOUT HANDLER =====
  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", (e) => {
      e.preventDefault();

      const confirmLogout = confirm("Are you sure you want to log out?");
      if (!confirmLogout) return;

      // Clear expenses from memory
      expenses = [];

      // TODO: Call backend logout endpoint when authentication is implemented
      console.log("Logout — backend authentication to be implemented.");

      // Redirect back to login page
      window.location.href = "index.html";
    });
  }
});
