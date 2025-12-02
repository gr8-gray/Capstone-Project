package com.yourapp.expensetracker.expense_api.controller;

import com.yourapp.expensetracker.expense_api.model.Budget;
import com.yourapp.expensetracker.expense_api.model.User;
import com.yourapp.expensetracker.expense_api.service.AuthService;
import com.yourapp.expensetracker.expense_api.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "*")   // same style as your other controllers
public class BudgetController {

    private final BudgetService budgetService;
    private final AuthService authService;

    @Autowired
    public BudgetController(BudgetService budgetService, AuthService authService) {
        this.budgetService = budgetService;
        this.authService = authService;
    }

    /**
     * Create a new budget
     * POST /api/budgets
     */
    @PostMapping
    public ResponseEntity<?> createBudget(@Valid @RequestBody Budget budget) {
        try {
            // Validate end date is not before start date
            if (budget.getEndDate() != null && budget.getStartDate() != null 
                    && budget.getEndDate().isBefore(budget.getStartDate())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "End date must be after or equal to start date"));
            }

            User currentUser = authService.getCurrentUser();
            budget.setUser(currentUser);
            
            Budget saved = budgetService.createBudget(budget);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all budgets for current user
     * GET /api/budgets
     */
    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets() {
        User currentUser = authService.getCurrentUser();
        List<Budget> budgets = budgetService.getAllBudgetsByUserId(currentUser.getId());
        return ResponseEntity.ok(budgets);
    }

    /**
     * Get budget by ID
     * GET /api/budgets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBudgetById(@PathVariable Long id) {
        try {
            Optional<Budget> budget = budgetService.getBudgetById(id);
            return budget.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve budget"));
        }
    }

    /**
     * Get active budgets for current user (budgets that include today's date)
     * GET /api/budgets/active
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveBudgets() {
        try {
            User currentUser = authService.getCurrentUser();
            LocalDate today = LocalDate.now();
            List<Budget> budgets = budgetService.getActiveBudgetsForUserAndDate(currentUser.getId(), today);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve active budgets"));
        }
    }

    /**
     * Get budgets by category for current user
     * GET /api/budgets/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getBudgetsByCategory(@PathVariable String category) {
        try {
            User currentUser = authService.getCurrentUser();
            List<Budget> budgets = budgetService.getBudgetsByUserAndCategory(currentUser.getId(), category);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve budgets by category"));
        }
    }

    /**
     * Get budgets by date range for current user
     * GET /api/budgets/date-range?startDate=2025-01-01&endDate=2025-01-31
     */
    @GetMapping("/date-range")
    public ResponseEntity<?> getBudgetsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "startDate cannot be after endDate"));
            }

            User currentUser = authService.getCurrentUser();
            List<Budget> budgets = budgetService.getBudgetsByUserAndDateRange(currentUser.getId(), startDate, endDate);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve budgets by date range"));
        }
    }

    /**
     * Update a budget
     * PUT /api/budgets/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody Budget updatedBudget) {
        try {
            Budget saved = budgetService.updateBudget(id, updatedBudget);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update budget"));
        }
    }

    /**
     * Delete a budget
     * DELETE /api/budgets/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable Long id) {
        try {
            budgetService.deleteBudget(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete budget"));
        }
    }
}