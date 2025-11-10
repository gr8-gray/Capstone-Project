package com.yourapp.expensetracker.expense_api.repository;

import com.yourapp.expensetracker.expense_api.model.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

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
@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepositoryIntegrationTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
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
        expenseRepository.save(new Expense("Lunch", new BigDecimal("15.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Dinner", new BigDecimal("25.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Bus", new BigDecimal("5.00"), "Transportation", LocalDate.now()));

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

        expenseRepository.save(new Expense("Today", new BigDecimal("10.00"), "Food", today));
        expenseRepository.save(new Expense("Yesterday", new BigDecimal("20.00"), "Food", yesterday));
        expenseRepository.save(new Expense("Last week", new BigDecimal("30.00"), "Food", lastWeek));

        // When
        List<Expense> recentExpenses = expenseRepository.findByDateBetween(yesterday, today);

        // Then
        assertThat(recentExpenses).hasSize(2);
    }

    @Test
    void shouldFindExpensesByAmountGreaterThan() {
        // Given
        expenseRepository.save(new Expense("Small", new BigDecimal("5.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Medium", new BigDecimal("50.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Large", new BigDecimal("150.00"), "Food", LocalDate.now()));

        // When
        List<Expense> largeExpenses = expenseRepository.findByAmountGreaterThan(new BigDecimal("25.00"));

        // Then
        assertThat(largeExpenses).hasSize(2);
        assertThat(largeExpenses).allMatch(e -> e.getAmount().compareTo(new BigDecimal("25.00")) > 0);
    }

    @Test
    void shouldFindExpensesByAmountBetween() {
        // Given
        expenseRepository.save(new Expense("Small", new BigDecimal("5.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Medium", new BigDecimal("50.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Large", new BigDecimal("150.00"), "Food", LocalDate.now()));

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
        expenseRepository.save(new Expense("Starbucks coffee", new BigDecimal("5.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Coffee beans", new BigDecimal("15.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Lunch", new BigDecimal("12.00"), "Food", LocalDate.now()));

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

        expenseRepository.save(new Expense("Food today", new BigDecimal("10.00"), "Food", today));
        expenseRepository.save(new Expense("Food yesterday", new BigDecimal("15.00"), "Food", yesterday));
        expenseRepository.save(new Expense("Food last week", new BigDecimal("20.00"), "Food", lastWeek));
        expenseRepository.save(new Expense("Transport today", new BigDecimal("5.00"), "Transportation", today));

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
        Expense expense = expenseRepository.save(
            new Expense("To delete", new BigDecimal("25.00"), "Other", LocalDate.now())
        );
        Long expenseId = expense.getId();

        // When
        expenseRepository.deleteById(expenseId);

        // Then
        assertThat(expenseRepository.findById(expenseId)).isEmpty();
    }

    @Test
    void shouldCountExpenses() {
        // Given
        expenseRepository.save(new Expense("Expense 1", new BigDecimal("10.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Expense 2", new BigDecimal("20.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Expense 3", new BigDecimal("30.00"), "Food", LocalDate.now()));

        // When
        long count = expenseRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldUpdateExpense() {
        // Given
        Expense expense = expenseRepository.save(
            new Expense("Original", new BigDecimal("100.00"), "Food", LocalDate.now())
        );

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

        expenseRepository.save(new Expense("Today", new BigDecimal("10.00"), "Food", today));
        expenseRepository.save(new Expense("Day before", new BigDecimal("30.00"), "Food", dayBefore));
        expenseRepository.save(new Expense("Yesterday", new BigDecimal("20.00"), "Food", yesterday));

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
        expenseRepository.save(new Expense("Small", new BigDecimal("10.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Medium", new BigDecimal("50.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Large", new BigDecimal("100.00"), "Food", LocalDate.now()));
        expenseRepository.save(new Expense("Extra Large", new BigDecimal("200.00"), "Food", LocalDate.now()));

        // When
        List<Expense> topExpenses = expenseRepository.findTop3ByOrderByAmountDesc();

        // Then
        assertThat(topExpenses).hasSize(3);
        assertThat(topExpenses.get(0).getDescription()).isEqualTo("Extra Large");
        assertThat(topExpenses.get(1).getDescription()).isEqualTo("Large");
        assertThat(topExpenses.get(2).getDescription()).isEqualTo("Medium");
    }
}
