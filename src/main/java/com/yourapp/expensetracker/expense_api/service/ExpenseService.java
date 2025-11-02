package com.yourapp.expensetracker.expense_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourapp.expensetracker.expense_api.repository.ExpenseRepository;
import com.yourapp.expensetracker.expense_api.model.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Expense business logic
 * Handles data processing and business rules for expense operations
 * @author Eric Gray - Backend Developer
 */
@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    /**
     * Create a new expense
     */
    public Expense createExpense(Expense expense) {
        validateExpense(expense);
        return expenseRepository.save(expense);
    }

    /**
     * Get all expenses
     */
    @Transactional(readOnly = true)
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    /**
     * Get expense by ID
     */
    @Transactional(readOnly = true)
    public Optional<Expense> getExpenseById(Long id) {
        return expenseRepository.findById(id);
    }

    /**
     * Update an existing expense
     */
    public Expense updateExpense(Long id, Expense updatedExpense) {
        Optional<Expense> existingExpense = expenseRepository.findById(id);
        if (existingExpense.isPresent()) {
            Expense expense = existingExpense.get();
            expense.setDescription(updatedExpense.getDescription());
            expense.setAmount(updatedExpense.getAmount());
            expense.setCategory(updatedExpense.getCategory());
            expense.setDate(updatedExpense.getDate());
            validateExpense(expense);
            return expenseRepository.save(expense);
        } else {
            throw new RuntimeException("Expense not found with id: " + id);
        }
    }

    /**
     * Delete an expense
     */
    public void deleteExpense(Long id) {
        if (expenseRepository.existsById(id)) {
            expenseRepository.deleteById(id);
        } else {
            throw new RuntimeException("Expense not found with id: " + id);
        }
    }

    /**
     * Get expenses by category
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByCategory(String category) {
        return expenseRepository.findByCategory(category);
    }

    /**
     * Get expenses within date range
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByDateBetween(startDate, endDate);
    }

    /**
     * Get expenses by category within date range
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByCategoryAndDateRange(String category, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByCategoryAndDateBetween(category, startDate, endDate);
    }

    /**
     * Search expenses by description keyword
     */
    @Transactional(readOnly = true)
    public List<Expense> searchExpensesByDescription(String keyword) {
        return expenseRepository.findByDescriptionContainingIgnoreCase(keyword);
    }

    /**
     * Get total amount by category within date range
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByCategory(String category, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = expenseRepository.sumAmountByCategoryAndDateRange(category, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get total amount within date range
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmount(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = expenseRepository.sumAmountByDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get all distinct categories
     */
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return expenseRepository.findDistinctCategories();
    }

    /**
     * Get expenses by amount range
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return expenseRepository.findByAmountGreaterThanEqual(minAmount)
                .stream()
                .filter(expense -> expense.getAmount().compareTo(maxAmount) <= 0)
                .toList();
    }

    /**
     * Validate expense data
     */
    private void validateExpense(Expense expense) {
        if (expense.getAmount() != null && expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (expense.getDate() != null && expense.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the future");
        }
        if (expense.getDescription() != null && expense.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (expense.getCategory() != null && expense.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
    }

    // TODO: Add user-specific methods when User entity is implemented
    // public Expense createExpenseForUser(Expense newExpense, User user) {
    //     newExpense.setUser(user);
    //     validateExpense(newExpense);
    //     return expenseRepository.save(newExpense);
    // }
    // 
    // public List<Expense> getExpensesForUser(Long userId) {
    //     return expenseRepository.findByUserId(userId);
    // }
}