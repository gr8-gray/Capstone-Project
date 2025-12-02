package com.yourapp.expensetracker.expense_api.service;

import com.yourapp.expensetracker.expense_api.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourapp.expensetracker.expense_api.repository.ExpenseRepository;
import com.yourapp.expensetracker.expense_api.model.Expense;
import com.yourapp.expensetracker.expense_api.model.User;

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
    private final AuthService authService;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository, AuthService authService) {
        this.expenseRepository = expenseRepository;
        this.authService = authService;
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
     * Get all expenses for a user with pagination (RECOMMENDED)
     */
    @Transactional(readOnly = true)
    public Page<Expense> getAllExpensesByUserId(Long userId, Pageable pageable) {
        logger.debug("Fetching expenses for user {} with pagination", userId);
        Page<Expense> expenses = expenseRepository.findByUserId(userId, pageable);
        logger.debug("Retrieved {} expenses (page {} of {})", expenses.getNumberOfElements(), 
                    expenses.getNumber() + 1, expenses.getTotalPages());
        return expenses;
    }
    
    /**
     * Get all expenses for a user (use pagination version for better performance)
     */
    @Transactional(readOnly = true)
    public List<Expense> getAllExpenses() {
        logger.warn("Using non-paginated getAllExpenses - consider using pagination for better performance");
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
     * Get expenses by user and category with pagination
     */
    @Transactional(readOnly = true)
    public Page<Expense> getExpensesByUserIdAndCategory(Long userId, String category, Pageable pageable) {
        logger.debug("Fetching expenses for user {} and category: {} with pagination", userId, category);
        return expenseRepository.findByUserIdAndCategory(userId, category, pageable);
    }
    
    /**
     * Get expenses by user and category
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByUserIdAndCategory(Long userId, String category) {
        logger.debug("Fetching expenses for user {} and category: {}", userId, category);
        List<Expense> expenses = expenseRepository.findByUserIdAndCategoryWithUser(userId, category);
        logger.debug("Found {} expenses for user {} category: {}", expenses.size(), userId, category);
        return expenses;
    }

    /**
     * Get expenses by user within date range with pagination
     */
    @Transactional(readOnly = true)
    public Page<Expense> getExpensesByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate, pageable);
    }
    
    /**
     * Get expenses by user within date range
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndDateBetweenWithUser(userId, startDate, endDate);
    }

    /**
     * Get expenses by user, category and date range with pagination
     */
    @Transactional(readOnly = true)
    public Page<Expense> getExpensesByUserIdCategoryAndDateRange(Long userId, String category, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return expenseRepository.findByUserIdAndCategoryAndDateBetween(userId, category, startDate, endDate, pageable);
    }
    
    /**
     * Get expenses by user, category and date range
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByUserIdCategoryAndDateRange(Long userId, String category, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndCategoryAndDateBetweenWithUser(userId, category, startDate, endDate);
    }

    /**
     * Search expenses by user and description keyword with pagination
     */
    @Transactional(readOnly = true)
    public Page<Expense> searchExpensesByUserIdAndDescription(Long userId, String keyword, Pageable pageable) {
        return expenseRepository.findByUserIdAndDescriptionContainingIgnoreCase(userId, keyword, pageable);
    }
    
    /**
     * Search expenses by user and description keyword
     */
    @Transactional(readOnly = true)
    public List<Expense> searchExpensesByUserIdAndDescription(Long userId, String keyword) {
        return expenseRepository.findByUserIdAndDescriptionContainingIgnoreCaseWithUser(userId, keyword);
    }

    /**
     * Get total amount by user, category and date range
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByUserIdCategoryAndDateRange(Long userId, String category, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = expenseRepository.sumAmountByUserIdAndCategoryAndDateRange(userId, category, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get total amount by user within date range
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = expenseRepository.sumAmountByUserIdAndDateRange(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get all distinct categories for user
     */
    @Transactional(readOnly = true)
    public List<String> getAllCategoriesByUserId(Long userId) {
        return expenseRepository.findDistinctCategoriesByUserId(userId);
    }

    /**
     * Get expenses by user and amount range with pagination (OPTIMIZED - uses proper query)
     */
    @Transactional(readOnly = true)
    public Page<Expense> getExpensesByUserIdAndAmountRange(Long userId, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        return expenseRepository.findByUserIdAndAmountBetween(userId, minAmount, maxAmount, pageable);
    }
    
    /**
     * Get expenses by user and amount range (OPTIMIZED - uses proper query)
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByUserIdAndAmountRange(Long userId, BigDecimal minAmount, BigDecimal maxAmount) {
        return expenseRepository.findByUserIdAndAmountBetween(userId, minAmount, maxAmount);
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

    /**
     * Get all expenses by user (with JOIN FETCH to avoid N+1)
     */
    public List<Expense> getExpensesByUserId(Long userId) {
        return expenseRepository.findByUserIdWithUser(userId);
    }
    
    /**
     * Get expense count by user
     */
    @Transactional(readOnly = true)
    public long countExpensesByUserId(Long userId) {
        return expenseRepository.countByUserId(userId);
    }
    
    /**
     * Get expense count by user and category
     */
    @Transactional(readOnly = true)
    public long countExpensesByUserIdAndCategory(Long userId, String category) {
        return expenseRepository.countByUserIdAndCategory(userId, category);
    }
    
    /**
     * Get expense count by user and date range
     */
    @Transactional(readOnly = true)
    public long countExpensesByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.countByUserIdAndDateBetween(userId, startDate, endDate);
    }
    
    // ===== BACKWARD COMPATIBILITY METHODS FOR TESTS =====
    
    /**
     * @deprecated Use getExpensesByUserIdAndCategory with user ID instead
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByCategory(String category) {
        User currentUser = authService.getCurrentUser();
        return expenseRepository.findByUserIdAndCategoryWithUser(currentUser.getId(), category);
    }
    
    /**
     * @deprecated Use getExpensesByUserIdAndDateRange with user ID instead
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = authService.getCurrentUser();
        return expenseRepository.findByUserIdAndDateBetweenWithUser(currentUser.getId(), startDate, endDate);
    }
    
    /**
     * @deprecated Use getExpensesByUserIdAndAmountRange with user ID instead
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        User currentUser = authService.getCurrentUser();
        return expenseRepository.findByUserIdAndAmountBetween(currentUser.getId(), minAmount, maxAmount);
    }
}