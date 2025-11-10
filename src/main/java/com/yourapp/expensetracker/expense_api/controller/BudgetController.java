package com.yourapp.expensetracker.expense_api.controller;

import com.yourapp.expensetracker.expense_api.model.Budget;
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

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    /**
     * Create a new budget
     * POST /api/budgets
     */
    @PostMapping
    public ResponseEntity<?> createBudget(@Valid @RequestBody Budget budget) {
        try {
            Budget saved = budgetService.createBudget(budget);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all budgets
     * GET /api/budgets
     */
    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets() {
        List<Budget> budgets = budgetService.getAllBudgets();
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
     * Get budgets active in a date range
     * GET /api/budgets/active?startDate=2025-01-01&endDate=2025-01-31
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveBudgetsForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "startDate cannot be after endDate"));
            }

            List<Budget> budgets = budgetService.getActiveBudgetsForPeriod(startDate, endDate);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve active budgets"));
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