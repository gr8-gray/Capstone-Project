package com.yourapp.expensetracker.expense_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourapp.expensetracker.expense_api.BaseIntegrationTest;
import com.yourapp.expensetracker.expense_api.model.Budget;
import com.yourapp.expensetracker.expense_api.model.User;
import com.yourapp.expensetracker.expense_api.repository.BudgetRepository;
import com.yourapp.expensetracker.expense_api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BudgetController
 * Tests CRUD operations for budgets with authentication
 * @author Eric Gray - Backend Developer
 */
class BudgetControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        super.setUpBaseTest();  // Setup user and security context
        
        // Clean up
        budgetRepository.deleteAll();

        // Generate JWT token
        jwtToken = jwtTokenProvider.generateTokenFromUsername(testUser.getUsername());
    }

    @Test
    void shouldCreateBudget() throws Exception {
        // Given
        Map<String, Object> budgetRequest = new HashMap<>();
        budgetRequest.put("category", "Food & Dining");
        budgetRequest.put("limitAmount", 1000.00);
        budgetRequest.put("startDate", LocalDate.now().withDayOfMonth(1).toString());
        budgetRequest.put("endDate", LocalDate.now().withDayOfMonth(28).toString());

        // When & Then
        mockMvc.perform(post("/api/budgets")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budgetRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.category").value("Food & Dining"))
                .andExpect(jsonPath("$.limitAmount").value(1000.00))
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists());
    }

    @Test
    void shouldGetAllBudgets() throws Exception {
        // Given: Create test budgets
        createBudget("Food & Dining", new BigDecimal("1000.00"));
        createBudget("Transportation", new BigDecimal("500.00"));

        // When & Then
        mockMvc.perform(get("/api/budgets")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].category", containsInAnyOrder("Food & Dining", "Transportation")));
    }

    @Test
    void shouldGetBudgetById() throws Exception {
        // Given
        Budget budget = createBudget("Food & Dining", new BigDecimal("1000.00"));

        // When & Then
        mockMvc.perform(get("/api/budgets/" + budget.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budget.getId()))
                .andExpect(jsonPath("$.category").value("Food & Dining"))
                .andExpect(jsonPath("$.limitAmount").value(1000.00));
    }

    @Test
    void shouldReturnNotFoundForNonExistentBudget() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/budgets/99999")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateBudget() throws Exception {
        // Given: Existing budget
        Budget budget = createBudget("Food & Dining", new BigDecimal("1000.00"));

        // When: Update budget
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("category", "Food & Dining");
        updateRequest.put("limitAmount", 1500.00);
        updateRequest.put("startDate", budget.getStartDate().toString());
        updateRequest.put("endDate", budget.getEndDate().toString());

        // Then
        mockMvc.perform(put("/api/budgets/" + budget.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budget.getId()))
                .andExpect(jsonPath("$.limitAmount").value(1500.00));
    }

    @Test
    void shouldDeleteBudget() throws Exception {
        // Given
        Budget budget = createBudget("Food & Dining", new BigDecimal("1000.00"));

        // When: Delete budget
        mockMvc.perform(delete("/api/budgets/" + budget.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Then: Budget should be deleted
        mockMvc.perform(get("/api/budgets/" + budget.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetActiveBudgets() throws Exception {
        // Given: Budget with active date range that includes today
        Budget budget = new Budget();
        budget.setCategory("Food & Dining");
        budget.setLimitAmount(new BigDecimal("1000.00"));
        budget.setStartDate(LocalDate.now().minusDays(15));  // Starts 15 days ago
        budget.setEndDate(LocalDate.now().plusDays(15));     // Ends 15 days from now
        budget.setUser(testUser);
        budgetRepository.save(budget);

        // When & Then
        mockMvc.perform(get("/api/budgets/active")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void shouldGetBudgetsByCategory() throws Exception {
        // Given
        createBudget("Food & Dining", new BigDecimal("1000.00"));
        createBudget("Food & Dining", new BigDecimal("1200.00"));
        createBudget("Transportation", new BigDecimal("500.00"));

        // When & Then
        mockMvc.perform(get("/api/budgets/category/Food & Dining")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].category", everyItem(is("Food & Dining"))));
    }

    @Test
    void shouldRejectBudgetWithNegativeAmount() throws Exception {
        // Given
        Map<String, Object> budgetRequest = new HashMap<>();
        budgetRequest.put("category", "Food & Dining");
        budgetRequest.put("limitAmount", -100.00);
        budgetRequest.put("startDate", LocalDate.now().toString());
        budgetRequest.put("endDate", LocalDate.now().plusDays(30).toString());

        // When & Then
        mockMvc.perform(post("/api/budgets")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budgetRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectBudgetWithEndDateBeforeStartDate() throws Exception {
        // Given
        Map<String, Object> budgetRequest = new HashMap<>();
        budgetRequest.put("category", "Food & Dining");
        budgetRequest.put("limitAmount", 1000.00);
        budgetRequest.put("startDate", LocalDate.now().toString());
        budgetRequest.put("endDate", LocalDate.now().minusDays(10).toString());

        // When & Then
        mockMvc.perform(post("/api/budgets")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budgetRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        // Clear security context to simulate unauthenticated request
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        // When: Try to access without token
        // Note: Spring Security 6.x returns 403 (Forbidden) instead of 401 (Unauthorized)
        // when CSRF is disabled and there's no authentication
        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldOnlyReturnCurrentUsersBudgets() throws Exception {
        // Given: Create another user with budget
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPasswordHash(passwordEncoder.encode("password123"));
        otherUser.setRole("USER");
        otherUser.setActive(true);
        otherUser = userRepository.save(otherUser);

        Budget otherUserBudget = new Budget();
        otherUserBudget.setCategory("Other User Budget");
        otherUserBudget.setLimitAmount(new BigDecimal("500.00"));
        otherUserBudget.setStartDate(LocalDate.now().withDayOfMonth(1));
        otherUserBudget.setEndDate(LocalDate.now().withDayOfMonth(28));
        otherUserBudget.setUser(otherUser);  // Associate with other user
        budgetRepository.save(otherUserBudget);

        // Create current user's budget
        createBudget("My Budget", new BigDecimal("1000.00"));

        // When: Get all budgets
        mockMvc.perform(get("/api/budgets")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("My Budget"));
    }

    @Test
    void shouldGetBudgetsByDateRange() throws Exception {
        // Given: Budgets with different date ranges
        Budget currentBudget = createBudget("Current Budget", new BigDecimal("1000.00"));
        
        Budget futureBudget = new Budget();
        futureBudget.setCategory("Future Budget");
        futureBudget.setLimitAmount(new BigDecimal("500.00"));
        futureBudget.setStartDate(LocalDate.now().plusMonths(1));
        futureBudget.setEndDate(LocalDate.now().plusMonths(2));
        futureBudget.setUser(testUser);  // Associate with test user
        budgetRepository.save(futureBudget);

        // When: Get budgets for current month
        mockMvc.perform(get("/api/budgets/date-range")
                .header("Authorization", "Bearer " + jwtToken)
                .param("startDate", LocalDate.now().withDayOfMonth(1).toString())
                .param("endDate", LocalDate.now().withDayOfMonth(28).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void shouldRejectBudgetWithEmptyCategory() throws Exception {
        // Given
        Map<String, Object> budgetRequest = new HashMap<>();
        budgetRequest.put("category", "");
        budgetRequest.put("limitAmount", 1000.00);
        budgetRequest.put("startDate", LocalDate.now().toString());
        budgetRequest.put("endDate", LocalDate.now().plusDays(30).toString());

        // When & Then
        mockMvc.perform(post("/api/budgets")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budgetRequest)))
                .andExpect(status().isBadRequest());
    }

    // Helper method
    private Budget createBudget(String category, BigDecimal limitAmount) {
        Budget budget = new Budget();
        budget.setCategory(category);
        budget.setLimitAmount(limitAmount);
        budget.setStartDate(LocalDate.now().withDayOfMonth(1));
        budget.setEndDate(LocalDate.now().withDayOfMonth(28));
        budget.setUser(testUser);  // Associate with test user
        return budgetRepository.save(budget);
    }
}
