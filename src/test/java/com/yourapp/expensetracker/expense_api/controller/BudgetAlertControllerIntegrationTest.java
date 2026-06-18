package com.yourapp.expensetracker.expense_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourapp.expensetracker.expense_api.BaseIntegrationTest;
import com.yourapp.expensetracker.expense_api.model.*;
import com.yourapp.expensetracker.expense_api.repository.*;
import com.yourapp.expensetracker.expense_api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BudgetAlertController
 * Tests REST endpoints with authentication
 * @author Eric Gray - Backend Developer
 */
class BudgetAlertControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BudgetAlertRepository budgetAlertRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private Budget testBudget;

    @BeforeEach
    void setUp() {
        super.setUpBaseTest();  // Setup user and security context
        
        // Clean up
        budgetAlertRepository.deleteAll();
        expenseRepository.deleteAll();
        budgetRepository.deleteAll();

        // Generate JWT token
        jwtToken = jwtTokenProvider.generateTokenFromUsername(testUser.getUsername());

        // Create test budget
        testBudget = new Budget();
        testBudget.setCategory("Food & Dining");
        testBudget.setLimitAmount(new BigDecimal("1000.00"));
        testBudget.setStartDate(LocalDate.now().withDayOfMonth(1));
        testBudget.setEndDate(LocalDate.now().withDayOfMonth(28));
        testBudget.setUser(testUser);  // Associate budget with test user
        testBudget = budgetRepository.save(testBudget);
        budgetRepository.flush();
    }

    @Test
    void shouldCheckAllBudgetsAndReturnAlerts() throws Exception {
        // Given: Budget with 75% spending
        createExpense("Groceries", new BigDecimal("750.00"));

        // When: Check all budgets
        mockMvc.perform(post("/api/budget-alerts/check-all")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].alertLevel").value("WARNING"))
                .andExpect(jsonPath("$[0].budgetCategory").value("Food & Dining"))
                .andExpect(jsonPath("$[0].percentageUsed").value(75.00))
                .andExpect(jsonPath("$[0].spentAmount").value(750.00))
                .andExpect(jsonPath("$[0].budgetLimit").value(1000.00))
                .andExpect(jsonPath("$[0].isRead").value(false));
    }

    @Test
    void shouldCheckSpecificBudgetAndReturnAlert() throws Exception {
        // Given: Budget with 90% spending
        createExpense("Expensive meal", new BigDecimal("900.00"));

        // When: Check specific budget
        mockMvc.perform(post("/api/budget-alerts/check/" + testBudget.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertLevel").value("DANGER"))
                .andExpect(jsonPath("$.percentageUsed").value(90.00))
                .andExpect(jsonPath("$.message").value(containsString("DANGER")));
    }

    @Test
    void shouldReturnNoContentWhenNoAlertNeeded() throws Exception {
        // Given: Budget with only 40% spending (below threshold)
        createExpense("Small purchase", new BigDecimal("400.00"));

        // When: Check budget
        mockMvc.perform(post("/api/budget-alerts/check/" + testBudget.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetUnreadAlerts() throws Exception {
        // Given: Generate an alert
        createExpense("Groceries", new BigDecimal("750.00"));
        mockMvc.perform(post("/api/budget-alerts/check-all")
                .header("Authorization", "Bearer " + jwtToken));

        // When: Get unread alerts
        mockMvc.perform(get("/api/budget-alerts/unread")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].isRead").value(false));
    }

    @Test
    void shouldGetAlertsByLevel() throws Exception {
        // Given: Generate WARNING alert
        createExpense("Groceries", new BigDecimal("750.00"));
        mockMvc.perform(post("/api/budget-alerts/check-all")
                .header("Authorization", "Bearer " + jwtToken));

        // When: Get WARNING level alerts
        mockMvc.perform(get("/api/budget-alerts/level/WARNING")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].alertLevel", everyItem(is("WARNING"))));
    }

    @Test
    void shouldGetAlertsByBudgetId() throws Exception {
        // Given: Generate alert for specific budget
        createExpense("Groceries", new BigDecimal("750.00"));
        mockMvc.perform(post("/api/budget-alerts/check-all")
                .header("Authorization", "Bearer " + jwtToken));

        // When: Get alerts for budget
        mockMvc.perform(get("/api/budget-alerts/budget/" + testBudget.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].budgetId").value(testBudget.getId()));
    }

    @Test
    void shouldMarkAlertAsRead() throws Exception {
        // Given: Generate an alert
        createExpense("Groceries", new BigDecimal("750.00"));
        mockMvc.perform(post("/api/budget-alerts/check-all")
                .header("Authorization", "Bearer " + jwtToken));

        // Get alert ID
        BudgetAlert alert = budgetAlertRepository.findAll().get(0);

        // When: Mark as read
        mockMvc.perform(put("/api/budget-alerts/" + alert.getId() + "/read")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true))
                .andExpect(jsonPath("$.readAt").exists());
    }

    @Test
    void shouldDeleteAlert() throws Exception {
        // Given: Generate an alert
        createExpense("Groceries", new BigDecimal("750.00"));
        mockMvc.perform(post("/api/budget-alerts/check-all")
                .header("Authorization", "Bearer " + jwtToken));

        BudgetAlert alert = budgetAlertRepository.findAll().get(0);

        // When: Delete alert
        mockMvc.perform(delete("/api/budget-alerts/" + alert.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Then: Alert deleted
        mockMvc.perform(get("/api/budget-alerts/unread")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldGetUnreadAlertCount() throws Exception {
        // Given: Generate multiple alerts
        createExpense("Purchase 1", new BigDecimal("500.00"));
        createExpense("Purchase 2", new BigDecimal("250.00"));
        mockMvc.perform(post("/api/budget-alerts/check-all")
                .header("Authorization", "Bearer " + jwtToken));

        // When: Get unread count
        mockMvc.perform(get("/api/budget-alerts/count/unread")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(greaterThan(0)));
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        // As an anonymous user (no JWT) protected endpoints must be rejected.
        // .with(anonymous()) pins the request's security context regardless of any
        // ambient test context. Spring Security 6.x returns 403 when CSRF is disabled.
        mockMvc.perform(post("/api/budget-alerts/check-all").with(anonymous()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/budget-alerts/unread").with(anonymous()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldHandleInvalidBudgetId() throws Exception {
        // When: Check non-existent budget
        mockMvc.perform(post("/api/budget-alerts/check/99999")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleInvalidAlertId() throws Exception {
        // When: Mark non-existent alert as read
        mockMvc.perform(put("/api/budget-alerts/99999/read")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGenerateCriticalAlertWhenBudgetExceeded() throws Exception {
        // Given: Budget exceeded
        createExpense("Expensive purchase", new BigDecimal("1200.00"));

        // When: Check budget
        mockMvc.perform(post("/api/budget-alerts/check-all")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alertLevel").value("CRITICAL"))
                .andExpect(jsonPath("$[0].percentageUsed", greaterThan(100.0)))
                .andExpect(jsonPath("$[0].message").value(containsString("CRITICAL")))
                .andExpect(jsonPath("$[0].message").value(containsString("exceeded")));
    }

    @Test
    void shouldHandleMultipleBudgetsWithDifferentAlertLevels() throws Exception {
        // Given: Create another budget
        Budget budget2 = new Budget();
        budget2.setCategory("Transportation");
        budget2.setLimitAmount(new BigDecimal("500.00"));
        budget2.setStartDate(LocalDate.now().withDayOfMonth(1));
        budget2.setEndDate(LocalDate.now().withDayOfMonth(28));
        budget2.setUser(testUser);  // Associate budget with test user
        budgetRepository.save(budget2);

        // Food budget at 75% (WARNING)
        createExpense("Groceries", new BigDecimal("750.00"));

        // Transportation at 90% (DANGER)
        Expense transportExpense = new Expense();
        transportExpense.setUser(testUser);
        transportExpense.setDescription("Gas");
        transportExpense.setAmount(new BigDecimal("450.00"));
        transportExpense.setCategory("Transportation");
        transportExpense.setDate(LocalDate.now());
        expenseRepository.save(transportExpense);

        // When: Check all budgets
        mockMvc.perform(post("/api/budget-alerts/check-all")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].alertLevel", containsInAnyOrder("WARNING", "DANGER")));
    }

    // Helper method
    private Expense createExpense(String description, BigDecimal amount) {
        Expense expense = new Expense();
        expense.setUser(testUser);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategory("Food & Dining");
        expense.setDate(LocalDate.now());
        return expenseRepository.save(expense);
    }
}
