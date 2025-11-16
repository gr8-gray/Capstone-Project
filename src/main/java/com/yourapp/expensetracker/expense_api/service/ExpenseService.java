package com.yourapp.expensetracker.expense_api.service;

import com.yourapp.expensetracker.expense_api.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @author Michael Basye - Database Engineer (Added logging)
 */
@Service
@Transactional
public class ExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    /**
     * Create a new expense
     */
    public Expense createExpense(Expense expense) {
        logger.debug("Creating new expense: category={}, amount={}", 
                    expense.getCategory(), expense.getAmount());
        validateExpense(expense);
        Expense savedExpense = expenseRepository.save(expense);
        logger.info("Successfully created expense with ID: {}", savedExpense.getId());
        return savedExpense;
    }

    /**
     * Get all expenses
     */
    @Transactional(readOnly = true)
    public List<Expense> getAllExpenses() {
        logger.debug("Fetching all expenses");
        List<Expense> expenses = expenseRepository.findAll();
        logger.debug("Retrieved {} expenses", expenses.size());
        return expenses;
    }

    /**
     * Get expense by ID
     */
    @Transactional(readOnly = true)
    public Optional<Expense> getExpenseById(Long id) {
        logger.debug("Fetching expense with ID: {}", id);
        Optional<Expense> expense = expenseRepository.findById(id);
        if (expense.isEmpty()) {
            logger.warn("Expense not found with ID: {}", id);
        }
        return expense;
    }

    /**
     * Update an existing expense
     */
    public Expense updateExpense(Long id, Expense updatedExpense) {
        logger.debug("Updating expense with ID: {}", id);
        Optional<Expense> existingExpense = expenseRepository.findById(id);
        if (existingExpense.isPresent()) {
            Expense expense = existingExpense.get();
            expense.setDescription(updatedExpense.getDescription());
            expense.setAmount(updatedExpense.getAmount());
            expense.setCategory(updatedExpense.getCategory());
            expense.setDate(updatedExpense.getDate());
            validateExpense(expense);
            Expense saved = expenseRepository.save(expense);
            logger.info("Successfully updated expense with ID: {}", id);
            return saved;
        } else {
            logger.error("Failed to update expense - ID not found: {}", id);
            throw new ResourceNotFoundException("Expense", "id", id);
        }
    }

    /**
     * Delete an expense
     */
    public void deleteExpense(Long id) {
        logger.debug("Attempting to delete expense with ID: {}", id);
        if (expenseRepository.existsById(id)) {
            expenseRepository.deleteById(id);
            logger.info("Successfully deleted expense with ID: {}", id);
        } else {
            logger.error("Failed to delete expense - ID not found: {}", id);
            throw new ResourceNotFoundException("Expense", "id", id);
        }
    }

    /**
     * Get expenses by category
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByCategory(String category) {
        logger.debug("Fetching expenses for category: {}", category);
        List<Expense> expenses = expenseRepository.findByCategory(category);
        logger.debug("Found {} expenses for category: {}", expenses.size(), category);
        return expenses;
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
        logger.trace("Validating expense data");
        if (expense.getAmount() != null && expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Validation failed: Amount must be positive, got: {}", expense.getAmount());
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (expense.getDate() != null && expense.getDate().isAfter(LocalDate.now())) {
            logger.warn("Validation failed: Future date not allowed, got: {}", expense.getDate());
            throw new IllegalArgumentException("Date cannot be in the future");
        }
        if (expense.getDescription() != null && expense.getDescription().trim().isEmpty()) {
            logger.warn("Validation failed: Description cannot be empty");
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (expense.getCategory() != null && expense.getCategory().trim().isEmpty()) {
            logger.warn("Validation failed: Category cannot be empty");
            throw new IllegalArgumentException("Category cannot be empty");
        }
        logger.trace("Expense validation passed");
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