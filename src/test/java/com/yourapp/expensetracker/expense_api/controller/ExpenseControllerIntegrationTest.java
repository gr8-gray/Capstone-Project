package com.yourapp.expensetracker.expense_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourapp.expensetracker.expense_api.model.Expense;
import com.yourapp.expensetracker.expense_api.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ExpenseController
 * Tests full request/response cycle with actual Spring context
 * 
 * @author Eric Gray - Backend Developer
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        expenseRepository.deleteAll();
    }

    @Test
    void shouldCreateExpense() throws Exception {
        // Given
        Expense expense = new Expense(
            "Grocery shopping",
            new BigDecimal("125.50"),
            "Food",
            LocalDate.now()
        );

        // When & Then
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Grocery shopping"))
                .andExpect(jsonPath("$.amount").value(125.50))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.date").exists());
    }

    @Test
    void shouldGetAllExpenses() throws Exception {
        // Given - Create test data
        Expense expense1 = new Expense("Coffee", new BigDecimal("5.50"), "Food", LocalDate.now());
        Expense expense2 = new Expense("Gas", new BigDecimal("45.00"), "Transportation", LocalDate.now());
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        // When & Then
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description").value("Coffee"))
                .andExpect(jsonPath("$[1].description").value("Gas"));
    }

    @Test
    void shouldGetExpenseById() throws Exception {
        // Given
        Expense expense = new Expense("Movie tickets", new BigDecimal("30.00"), "Entertainment", LocalDate.now());
        Expense savedExpense = expenseRepository.save(expense);

        // When & Then
        mockMvc.perform(get("/api/expenses/" + savedExpense.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedExpense.getId()))
                .andExpect(jsonPath("$.description").value("Movie tickets"))
                .andExpect(jsonPath("$.amount").value(30.00))
                .andExpect(jsonPath("$.category").value("Entertainment"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentExpense() throws Exception {
        mockMvc.perform(get("/api/expenses/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateExpense() throws Exception {
        // Given - Create existing expense
        Expense existingExpense = new Expense("Old description", new BigDecimal("100.00"), "Food", LocalDate.now());
        Expense savedExpense = expenseRepository.save(existingExpense);

        // Update values
        savedExpense.setDescription("Updated description");
        savedExpense.setAmount(new BigDecimal("150.00"));
        savedExpense.setCategory("Shopping");

        // When & Then
        mockMvc.perform(put("/api/expenses/" + savedExpense.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedExpense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.category").value("Shopping"));
    }

    @Test
    void shouldDeleteExpense() throws Exception {
        // Given
        Expense expense = new Expense("To be deleted", new BigDecimal("50.00"), "Other", LocalDate.now());
        Expense savedExpense = expenseRepository.save(expense);

        // When & Then - Delete
        mockMvc.perform(delete("/api/expenses/" + savedExpense.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/expenses/" + savedExpense.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFilterExpensesByCategory() throws Exception {
        // Given
        expenseRepository.save(new Expense("Lunch", new BigDecimal("15.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Dinner", new BigDecimal("30.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Bus ticket", new BigDecimal("5.00"), "Transportation", LocalDate.now()));

        // When & Then
        mockMvc.perform(get("/api/expenses/category/Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].category", everyItem(is("Food"))));
    }

    @Test
    void shouldFilterExpensesByDateRange() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);

        expenseRepository.save(new Expense("Today expense", new BigDecimal("10.00"), "Food", today));
        expenseRepository.save(new Expense("Yesterday expense", new BigDecimal("20.00"), "Food", yesterday));
        expenseRepository.save(new Expense("Last week expense", new BigDecimal("30.00"), "Food", lastWeek));

        // When & Then - Get expenses from yesterday to today
        mockMvc.perform(get("/api/expenses/date-range")
                .param("startDate", yesterday.toString())
                .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldSearchExpensesByDescription() throws Exception {
        // Given
        expenseRepository.save(new Expense("Coffee at Starbucks", new BigDecimal("5.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Coffee beans", new BigDecimal("15.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Lunch", new BigDecimal("12.00"), "Food", LocalDate.now()));

        // When & Then
        mockMvc.perform(get("/api/expenses/search")
                .param("keyword", "Coffee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].description", everyItem(containsString("Coffee"))));
    }

    @Test
    void shouldRejectInvalidExpense() throws Exception {
        // Given - Invalid expense (negative amount)
        Expense invalidExpense = new Expense("Invalid", new BigDecimal("-10.00"), "Food", LocalDate.now());

        // When & Then
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidExpense)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectExpenseWithoutDescription() throws Exception {
        // Given - Expense without description
        Expense invalidExpense = new Expense("", new BigDecimal("10.00"), "Food", LocalDate.now());

        // When & Then
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidExpense)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectExpenseWithoutCategory() throws Exception {
        // Given - Expense without category
        Expense invalidExpense = new Expense("Test", new BigDecimal("10.00"), "", LocalDate.now());

        // When & Then
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidExpense)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetExpensesByAmountRange() throws Exception {
        // Given
        expenseRepository.save(new Expense("Small purchase", new BigDecimal("5.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Medium purchase", new BigDecimal("50.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Large purchase", new BigDecimal("150.00"), "Food", LocalDate.now()));

        // When & Then - Get expenses between $10 and $100
        mockMvc.perform(get("/api/expenses/amount-range")
                .param("minAmount", "10.00")
                .param("maxAmount", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Medium purchase"));
    }
}
