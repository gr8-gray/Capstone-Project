package com.yourapp.expensetracker.expense_api.service;

import com.yourapp.expensetracker.expense_api.BaseIntegrationTest;
import com.yourapp.expensetracker.expense_api.model.Expense;
import com.yourapp.expensetracker.expense_api.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ExpenseService
 * Tests business logic with actual database interactions
 * Note: Some tests use methods that need to be implemented in ExpenseService
 * 
 * @author Eric Gray - Backend Developer
 */
class ExpenseServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        super.setUpBaseTest();
        expenseRepository.deleteAll();
    }

    @Test
    void shouldCreateExpense() {
        // Given
        Expense expense = new Expense("Lunch", new BigDecimal("15.99"), "Food", LocalDate.now());
        expense.setUser(testUser);

        // When
        Expense savedExpense = expenseService.createExpense(expense);

        // Then
        assertThat(savedExpense).isNotNull();
        assertThat(savedExpense.getId()).isNotNull();
        assertThat(savedExpense.getDescription()).isEqualTo("Lunch");
        assertThat(savedExpense.getAmount()).isEqualByComparingTo(new BigDecimal("15.99"));
        assertThat(savedExpense.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRetrieveAllExpenses() {
        // Given
        Expense e1 = new Expense("Coffee", new BigDecimal("5.00"), "Food", LocalDate.now());
        e1.setUser(testUser);
        expenseService.createExpense(e1);
        
        Expense e2 = new Expense("Gas", new BigDecimal("40.00"), "Transportation", LocalDate.now());
        e2.setUser(testUser);
        expenseService.createExpense(e2);
        
        Expense e3 = new Expense("Movie", new BigDecimal("15.00"), "Entertainment", LocalDate.now());
        e3.setUser(testUser);
        expenseService.createExpense(e3);

        // When
        List<Expense> expenses = expenseService.getAllExpenses();

        // Then
        assertThat(expenses).hasSize(3);
    }

    @Test
    void shouldRetrieveExpenseById() {
        // Given
        Expense expense = new Expense("Gym membership", new BigDecimal("50.00"), "Health", LocalDate.now());
        expense.setUser(testUser);
        expense = expenseService.createExpense(expense);

        // When
        Optional<Expense> retrievedExpense = expenseService.getExpenseById(expense.getId());

        // Then
        assertThat(retrievedExpense).isPresent();
        assertThat(retrievedExpense.get().getDescription()).isEqualTo("Gym membership");
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        // When
        Optional<Expense> expense = expenseService.getExpenseById(99999L);

        // Then
        assertThat(expense).isEmpty();
    }

    @Test
    void shouldUpdateExpense() {
        // Given
        Expense expense = new Expense("Old description", new BigDecimal("100.00"), "Food", LocalDate.now());
        expense.setUser(testUser);
        expense = expenseService.createExpense(expense);

        // When
        expense.setDescription("Updated description");
        expense.setAmount(new BigDecimal("150.00"));
        Expense updatedExpense = expenseService.updateExpense(expense.getId(), expense);

        // Then
        assertThat(updatedExpense.getDescription()).isEqualTo("Updated description");
        assertThat(updatedExpense.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(updatedExpense.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldDeleteExpense() {
        // Given
        Expense expense = new Expense("To be deleted", new BigDecimal("25.00"), "Other", LocalDate.now());
        expense.setUser(testUser);
        expense = expenseService.createExpense(expense);
        Long expenseId = expense.getId();

        // When
        expenseService.deleteExpense(expenseId);

        // Then
        Optional<Expense> deletedExpense = expenseService.getExpenseById(expenseId);
        assertThat(deletedExpense).isEmpty();
    }

    @Test
    void shouldFindExpensesByCategory() {
        // Given
        Expense e1 = new Expense("Lunch", new BigDecimal("15.00"), "Food", LocalDate.now());
        e1.setUser(testUser);
        expenseService.createExpense(e1);
        
        Expense e2 = new Expense("Dinner", new BigDecimal("30.00"), "Food", LocalDate.now());
        e2.setUser(testUser);
        expenseService.createExpense(e2);
        
        Expense e3 = new Expense("Bus ticket", new BigDecimal("5.00"), "Transportation", LocalDate.now());
        e3.setUser(testUser);
        expenseService.createExpense(e3);

        // When
        List<Expense> foodExpenses = expenseService.getExpensesByCategory("Food");

        // Then
        assertThat(foodExpenses).hasSize(2);
        assertThat(foodExpenses).allMatch(e -> e.getCategory().equals("Food"));
    }

    @Test
    void shouldFindExpensesByDateRange() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate lastWeek = today.minusDays(7);

        Expense e1 = new Expense("Today", new BigDecimal("10.00"), "Food", today);
        e1.setUser(testUser);
        expenseService.createExpense(e1);
        
        Expense e2 = new Expense("Yesterday", new BigDecimal("20.00"), "Food", yesterday);
        e2.setUser(testUser);
        expenseService.createExpense(e2);
        
        Expense e3 = new Expense("Two days ago", new BigDecimal("30.00"), "Food", twoDaysAgo);
        e3.setUser(testUser);
        expenseService.createExpense(e3);
        
        Expense e4 = new Expense("Last week", new BigDecimal("40.00"), "Food", lastWeek);
        e4.setUser(testUser);
        expenseService.createExpense(e4);

        // When
        List<Expense> recentExpenses = expenseService.getExpensesByDateRange(twoDaysAgo, today);

        // Then
        assertThat(recentExpenses).hasSize(3);
    }

    @Test
    void shouldFindExpensesByAmountRange() {
        // Given
        Expense e1 = new Expense("Small", new BigDecimal("5.00"), "Food", LocalDate.now());
        e1.setUser(testUser);
        expenseService.createExpense(e1);
        
        Expense e2 = new Expense("Medium", new BigDecimal("50.00"), "Food", LocalDate.now());
        e2.setUser(testUser);
        expenseService.createExpense(e2);
        
        Expense e3 = new Expense("Large", new BigDecimal("150.00"), "Food", LocalDate.now());
        e3.setUser(testUser);
        expenseService.createExpense(e3);

        // When
        List<Expense> midRangeExpenses = expenseService.getExpensesByAmountRange(
            new BigDecimal("10.00"), 
            new BigDecimal("100.00")
        );

        // Then
        assertThat(midRangeExpenses).hasSize(1);
        assertThat(midRangeExpenses.get(0).getDescription()).isEqualTo("Medium");
    }

    @Test
    void shouldThrowExceptionForInvalidExpense() {
        // Given - Expense with negative amount
        Expense invalidExpense = new Expense("Invalid", new BigDecimal("-10.00"), "Food", LocalDate.now());
        invalidExpense.setUser(testUser);

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(invalidExpense))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForExpenseWithoutDescription() {
        // Given
        Expense invalidExpense = new Expense("", new BigDecimal("10.00"), "Food", LocalDate.now());
        invalidExpense.setUser(testUser);

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(invalidExpense))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForExpenseWithoutCategory() {
        // Given
        Expense invalidExpense = new Expense("Test", new BigDecimal("10.00"), "", LocalDate.now());
        invalidExpense.setUser(testUser);

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(invalidExpense))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHandleConcurrentUpdates() throws InterruptedException {
        // Given
        Expense expense = new Expense("Concurrent test", new BigDecimal("100.00"), "Food", LocalDate.now());
        expense.setUser(testUser);
        expense = expenseService.createExpense(expense);

        // When - Simulate concurrent updates
        expense.setDescription("Update 1");
        Expense updated1 = expenseService.updateExpense(expense.getId(), expense);

        // Add small delay to ensure different timestamps
        Thread.sleep(10);

        expense.setDescription("Update 2");
        Expense updated2 = expenseService.updateExpense(expense.getId(), expense);

        // Then
        assertThat(updated2.getDescription()).isEqualTo("Update 2");
        assertThat(updated2.getUpdatedAt()).isAfterOrEqualTo(updated1.getUpdatedAt());
    }
}
