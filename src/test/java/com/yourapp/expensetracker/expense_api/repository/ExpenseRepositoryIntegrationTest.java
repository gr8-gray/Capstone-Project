package com.yourapp.expensetracker.expense_api.repository;

import com.yourapp.expensetracker.expense_api.BaseIntegrationTest;
import com.yourapp.expensetracker.expense_api.model.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ExpenseRepository
 * Tests custom query methods and database interactions
 * 
 * @author Eric Gray - Backend Developer
 */
class ExpenseRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        super.setUpBaseTest();
        expenseRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveExpense() {
        // Given
        Expense expense = new Expense(
            "Test Expense",
            new BigDecimal("50.00"),
            "Food",
            LocalDate.now()
        );
        expense.setUser(testUser);

        // When
        Expense savedExpense = expenseRepository.save(expense);
        Expense retrievedExpense = expenseRepository.findById(savedExpense.getId()).orElse(null);

        // Then
        assertThat(retrievedExpense).isNotNull();
        assertThat(retrievedExpense.getDescription()).isEqualTo("Test Expense");
        assertThat(retrievedExpense.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void shouldFindExpensesByCategory() {
        // Given
        Expense e1 = new Expense("Lunch", new BigDecimal("15.00"), "Food", LocalDate.now());
        e1.setUser(testUser);
        expenseRepository.save(e1);
        
        Expense e2 = new Expense("Dinner", new BigDecimal("25.00"), "Food", LocalDate.now());
        e2.setUser(testUser);
        expenseRepository.save(e2);
        
        Expense e3 = new Expense("Bus", new BigDecimal("5.00"), "Transportation", LocalDate.now());
        e3.setUser(testUser);
        expenseRepository.save(e3);

        // When
        List<Expense> foodExpenses = expenseRepository.findByCategory("Food");

        // Then
        assertThat(foodExpenses).hasSize(2);
        assertThat(foodExpenses).allMatch(e -> e.getCategory().equals("Food"));
    }

    @Test
    void shouldFindExpensesByDateBetween() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);

        Expense e1 = new Expense("Today", new BigDecimal("10.00"), "Food", today);
        e1.setUser(testUser);
        expenseRepository.save(e1);
        
        Expense e2 = new Expense("Yesterday", new BigDecimal("20.00"), "Food", yesterday);
        e2.setUser(testUser);
        expenseRepository.save(e2);
        
        Expense e3 = new Expense("Last week", new BigDecimal("30.00"), "Food", lastWeek);
        e3.setUser(testUser);
        expenseRepository.save(e3);

        // When
        List<Expense> recentExpenses = expenseRepository.findByDateBetween(yesterday, today);

        // Then
        assertThat(recentExpenses).hasSize(2);
    }

    @Test
    void shouldFindExpensesByAmountGreaterThan() {
        // Given
        Expense e1 = new Expense("Small", new BigDecimal("5.00"), "Food", LocalDate.now());
        e1.setUser(testUser);
        expenseRepository.save(e1);
        
        Expense e2 = new Expense("Medium", new BigDecimal("50.00"), "Food", LocalDate.now());
        e2.setUser(testUser);
        expenseRepository.save(e2);
        
        Expense e3 = new Expense("Large", new BigDecimal("150.00"), "Food", LocalDate.now());
        e3.setUser(testUser);
        expenseRepository.save(e3);

        // When
        List<Expense> largeExpenses = expenseRepository.findByAmountGreaterThan(new BigDecimal("25.00"));

        // Then
        assertThat(largeExpenses).hasSize(2);
        assertThat(largeExpenses).allMatch(e -> e.getAmount().compareTo(new BigDecimal("25.00")) > 0);
    }

    @Test
    void shouldFindExpensesByAmountBetween() {
        // Given
        Expense e1 = new Expense("Small", new BigDecimal("5.00"), "Food", LocalDate.now());
        e1.setUser(testUser);
        expenseRepository.save(e1);
        
        Expense e2 = new Expense("Medium", new BigDecimal("50.00"), "Food", LocalDate.now());
        e2.setUser(testUser);
        expenseRepository.save(e2);
        
        Expense e3 = new Expense("Large", new BigDecimal("150.00"), "Food", LocalDate.now());
        e3.setUser(testUser);
        expenseRepository.save(e3);

        // When
        List<Expense> midRangeExpenses = expenseRepository.findByAmountBetween(
            new BigDecimal("10.00"),
            new BigDecimal("100.00")
        );

        // Then
        assertThat(midRangeExpenses).hasSize(1);
        assertThat(midRangeExpenses.get(0).getDescription()).isEqualTo("Medium");
    }

    @Test
    void shouldFindExpensesByDescriptionContaining() {
        // Given
        Expense e1 = new Expense("Starbucks coffee", new BigDecimal("5.00"), "Food", LocalDate.now());
        e1.setUser(testUser);
        expenseRepository.save(e1);
        
        Expense e2 = new Expense("Coffee beans", new BigDecimal("15.00"), "Food", LocalDate.now());
        e2.setUser(testUser);
        expenseRepository.save(e2);
        
        Expense e3 = new Expense("Lunch", new BigDecimal("12.00"), "Food", LocalDate.now());
        e3.setUser(testUser);
        expenseRepository.save(e3);

        // When
        List<Expense> coffeeExpenses = expenseRepository.findByDescriptionContainingIgnoreCase("coffee");

        // Then
        assertThat(coffeeExpenses).hasSize(2);
        assertThat(coffeeExpenses).allMatch(e -> 
            e.getDescription().toLowerCase().contains("coffee")
        );
    }

    @Test
    void shouldFindExpensesByCategoryAndDateBetween() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);

        Expense e1 = new Expense("Food today", new BigDecimal("10.00"), "Food", today);
        e1.setUser(testUser);
        expenseRepository.save(e1);
        
        Expense e2 = new Expense("Food yesterday", new BigDecimal("15.00"), "Food", yesterday);
        e2.setUser(testUser);
        expenseRepository.save(e2);
        
        Expense e3 = new Expense("Food last week", new BigDecimal("20.00"), "Food", lastWeek);
        e3.setUser(testUser);
        expenseRepository.save(e3);
        
        Expense e4 = new Expense("Transport today", new BigDecimal("5.00"), "Transportation", today);
        e4.setUser(testUser);
        expenseRepository.save(e4);

        // When
        List<Expense> recentFoodExpenses = expenseRepository.findByCategoryAndDateBetween(
            "Food", yesterday, today
        );

        // Then
        assertThat(recentFoodExpenses).hasSize(2);
        assertThat(recentFoodExpenses).allMatch(e -> 
            e.getCategory().equals("Food") && 
            !e.getDate().isBefore(yesterday) && 
            !e.getDate().isAfter(today)
        );
    }

    @Test
    void shouldDeleteExpense() {
        // Given
        Expense expense = new Expense("To delete", new BigDecimal("25.00"), "Other", LocalDate.now());
        expense.setUser(testUser);
        expense = expenseRepository.save(expense);
        Long expenseId = expense.getId();

        // When
        expenseRepository.deleteById(expenseId);

        // Then
        assertThat(expenseRepository.findById(expenseId)).isEmpty();
    }

    @Test
    void shouldCountExpenses() {
        // Given
        Expense e1 = new Expense("Expense 1", new BigDecimal("10.00"), "Food", LocalDate.now());
        e1.setUser(testUser);
        expenseRepository.save(e1);
        
        Expense e2 = new Expense("Expense 2", new BigDecimal("20.00"), "Food", LocalDate.now());
        e2.setUser(testUser);
        expenseRepository.save(e2);
        
        Expense e3 = new Expense("Expense 3", new BigDecimal("30.00"), "Food", LocalDate.now());
        e3.setUser(testUser);
        expenseRepository.save(e3);

        // When
        long count = expenseRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldUpdateExpense() {
        // Given
        Expense expense = new Expense("Original", new BigDecimal("100.00"), "Food", LocalDate.now());
        expense.setUser(testUser);
        expense = expenseRepository.save(expense);

        // When
        expense.setDescription("Updated");
        expense.setAmount(new BigDecimal("150.00"));
        Expense updatedExpense = expenseRepository.save(expense);

        // Then
        assertThat(updatedExpense.getDescription()).isEqualTo("Updated");
        assertThat(updatedExpense.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldOrderExpensesByDateDescending() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate dayBefore = today.minusDays(2);

        Expense e1 = new Expense("Today", new BigDecimal("10.00"), "Food", today);
        e1.setUser(testUser);
        expenseRepository.save(e1);
        
        Expense e2 = new Expense("Day before", new BigDecimal("30.00"), "Food", dayBefore);
        e2.setUser(testUser);
        expenseRepository.save(e2);
        
        Expense e3 = new Expense("Yesterday", new BigDecimal("20.00"), "Food", yesterday);
        e3.setUser(testUser);
        expenseRepository.save(e3);

        // When
        List<Expense> expenses = expenseRepository.findAllByOrderByDateDesc();

        // Then
        assertThat(expenses).hasSize(3);
        assertThat(expenses.get(0).getDescription()).isEqualTo("Today");
        assertThat(expenses.get(1).getDescription()).isEqualTo("Yesterday");
        assertThat(expenses.get(2).getDescription()).isEqualTo("Day before");
    }

    @Test
    void shouldFindTopExpenses() {
        // Given
        Expense e1 = new Expense("Small", new BigDecimal("10.00"), "Food", LocalDate.now());
        e1.setUser(testUser);
        expenseRepository.save(e1);
        
        Expense e2 = new Expense("Medium", new BigDecimal("50.00"), "Food", LocalDate.now());
        e2.setUser(testUser);
        expenseRepository.save(e2);
        
        Expense e3 = new Expense("Large", new BigDecimal("100.00"), "Food", LocalDate.now());
        e3.setUser(testUser);
        expenseRepository.save(e3);
        
        Expense e4 = new Expense("Extra Large", new BigDecimal("200.00"), "Food", LocalDate.now());
        e4.setUser(testUser);
        expenseRepository.save(e4);

        // When
        List<Expense> topExpenses = expenseRepository.findTop3ByOrderByAmountDesc();

        // Then
        assertThat(topExpenses).hasSize(3);
        assertThat(topExpenses.get(0).getDescription()).isEqualTo("Extra Large");
        assertThat(topExpenses.get(1).getDescription()).isEqualTo("Large");
        assertThat(topExpenses.get(2).getDescription()).isEqualTo("Medium");
    }
}
