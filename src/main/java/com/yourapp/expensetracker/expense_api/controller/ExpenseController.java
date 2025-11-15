package com.yourapp.expensetracker.expense_api.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.yourapp.expensetracker.expense_api.model.Expense;
import com.yourapp.expensetracker.expense_api.service.ExpenseService;

import jakarta.validation.Valid;

/**
 * REST Controller for Expense operations
 * Handles HTTP requests for expense management
 * @author Eric Gray - Backend Developer
 */
@RestController
@RequestMapping("/api/expenses")
@Validated
@CrossOrigin(origins = "*") // TODO: Configure proper CORS in production
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Create a new expense
     * POST /api/expenses
     */
    @PostMapping
    public ResponseEntity<?> createExpense(@Valid @RequestBody Expense newExpense) {
        logger.info("Creating new expense: amount={}, category={}", newExpense.getAmount(), newExpense.getCategory());
        try {
            // TODO: Get the currently logged-in user (from Spring Security context)
            // SecurityContext context = SecurityContextHolder.getContext();
            // Authentication authentication = context.getAuthentication();
            
            Expense savedExpense = expenseService.createExpense(newExpense);
            logger.info("Expense created successfully with ID: {}", savedExpense.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedExpense);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create expense - validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to create expense: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create expense"));
        }
    }

    /**
     * Get all expenses
     * GET /api/expenses
     */
    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses() {
        logger.debug("Fetching all expenses");
        try {
            // TODO: Filter by currently logged-in user when User entity is implemented
            List<Expense> expenses = expenseService.getAllExpenses();
            logger.info("Retrieved {} expenses", expenses.size());
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            logger.error("Failed to fetch expenses: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get expense by ID
     * GET /api/expenses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getExpenseById(@PathVariable Long id) {
        try {
            Optional<Expense> expense = expenseService.getExpenseById(id);
            if (expense.isPresent()) {
                return ResponseEntity.ok(expense.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve expense"));
        }
    }

    /**
     * Update an existing expense
     * PUT /api/expenses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @Valid @RequestBody Expense updatedExpense) {
        try {
            // TODO: Verify user owns this expense when User entity is implemented
            Expense expense = expenseService.updateExpense(id, updatedExpense);
            return ResponseEntity.ok(expense);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update expense"));
        }
    }

    /**
     * Delete an expense
     * DELETE /api/expenses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id) {
        try {
            // TODO: Verify user owns this expense when User entity is implemented
            expenseService.deleteExpense(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete expense"));
        }
    }

    /**
     * Get expenses by category
     * GET /api/expenses/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Expense>> getExpensesByCategory(@PathVariable String category) {
        try {
            List<Expense> expenses = expenseService.getExpensesByCategory(category);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get expenses within date range
     * GET /api/expenses/date-range?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Expense>> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Expense> expenses = expenseService.getExpensesByDateRange(startDate, endDate);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search expenses by description
     * GET /api/expenses/search?keyword=grocery
     */
    @GetMapping("/search")
    public ResponseEntity<List<Expense>> searchExpenses(@RequestParam String keyword) {
        try {
            List<Expense> expenses = expenseService.searchExpensesByDescription(keyword);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all categories
     * GET /api/expenses/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        try {
            List<String> categories = expenseService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get total amount by category within date range
     * GET /api/expenses/total?category=Food&startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotalAmount(
            @RequestParam(required = false) String category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            BigDecimal total;
            if (category != null && !category.trim().isEmpty()) {
                total = expenseService.getTotalAmountByCategory(category, startDate, endDate);
            } else {
                total = expenseService.getTotalAmount(startDate, endDate);
            }
            return ResponseEntity.ok(Map.of("total", total));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get expenses by amount range
     * GET /api/expenses/amount-range?minAmount=10.00&maxAmount=100.00
     */
    @GetMapping("/amount-range")
    public ResponseEntity<List<Expense>> getExpensesByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {
        try {
            List<Expense> expenses = expenseService.getExpensesByAmountRange(minAmount, maxAmount);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}