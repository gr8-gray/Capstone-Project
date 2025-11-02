package com.yourapp.expensetracker.expense_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.yourapp.expensetracker.expense_api.model.Expense;
import com.yourapp.expensetracker.expense_api.service.ReportService;
import com.yourapp.expensetracker.expense_api.service.CategoryService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for generating expense reports and analytics
 * Provides endpoints for various reporting and data visualization needs
 * @author Eric Gray - Backend Developer
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*") // TODO: Configure proper CORS in production
public class ReportController {

    private final ReportService reportService;
    private final CategoryService categoryService;

    @Autowired
    public ReportController(ReportService reportService, CategoryService categoryService) {
        this.reportService = reportService;
        this.categoryService = categoryService;
    }

    /**
     * Get monthly expense summary
     * GET /api/reports/monthly?year=2025&month=10
     */
    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month) {
        try {
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest().build();
            }
            
            Map<String, Object> report = reportService.getMonthlyExpenseSummary(year, month);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get category-wise expense report
     * GET /api/reports/category?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/category")
    public ResponseEntity<Map<String, Object>> getCategoryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            
            Map<String, Object> report = reportService.getCategoryReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get expense trends over time
     * GET /api/reports/trends?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getExpenseTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            
            Map<String, Object> trends = reportService.getExpenseTrends(startDate, endDate);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get spending comparison between two periods
     * GET /api/reports/comparison?currentStart=2025-10-01&currentEnd=2025-10-31&previousStart=2025-09-01&previousEnd=2025-09-30
     */
    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> getSpendingComparison(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentEnd,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate previousStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate previousEnd) {
        try {
            if (currentStart.isAfter(currentEnd) || previousStart.isAfter(previousEnd)) {
                return ResponseEntity.badRequest().build();
            }
            
            Map<String, Object> comparison = reportService.getSpendingComparison(
                currentStart, currentEnd, previousStart, previousEnd);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get expenses above average
     * GET /api/reports/above-average?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/above-average")
    public ResponseEntity<List<Expense>> getExpensesAboveAverage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Expense> expenses = reportService.getExpensesAboveAverage(startDate, endDate);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all available categories
     * GET /api/reports/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAvailableCategories() {
        try {
            List<String> categories = categoryService.getDefaultCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get category suggestion based on description
     * GET /api/reports/suggest-category?description=grocery shopping
     */
    @GetMapping("/suggest-category")
    public ResponseEntity<Map<String, String>> suggestCategory(@RequestParam String description) {
        try {
            String suggestedCategory = categoryService.suggestCategory(description);
            return ResponseEntity.ok(Map.of("suggestedCategory", suggestedCategory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
