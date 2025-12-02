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

    /**
     * Get yearly expense report
     * GET /api/reports/yearly?year=2025
     */
    @GetMapping("/yearly")
    public ResponseEntity<Map<String, Object>> getYearlyReport(@RequestParam int year) {
        try {
            Map<String, Object> report = reportService.getYearlyReport(year);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get category breakdown for a date range
     * GET /api/reports/category-breakdown?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/category-breakdown")
    public ResponseEntity<Map<String, ?>> getCategoryBreakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            Map<String, ?> breakdown = reportService.getCategoryBreakdown(startDate, endDate);
            return ResponseEntity.ok(breakdown);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get total expenses for a date range
     * GET /api/reports/total?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> getTotalForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            java.math.BigDecimal total = reportService.getTotalForDateRange(startDate, endDate);
            return ResponseEntity.ok(Map.of("total", total, "startDate", startDate, "endDate", endDate));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get top expense categories for a date range
     * GET /api/reports/top-categories?startDate=2025-01-01&endDate=2025-12-31&limit=5
     */
    @GetMapping("/top-categories")
    public ResponseEntity<Map<String, ?>> getTopExpenseCategories(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            if (limit < 1) {
                return ResponseEntity.badRequest().build();
            }
            Map<String, ?> topCategories = reportService.getTopExpenseCategories(startDate, endDate, limit);
            return ResponseEntity.ok(topCategories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get average daily expenses for a date range
     * GET /api/reports/average-daily?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/average-daily")
    public ResponseEntity<Map<String, Object>> getAverageDailyExpense(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            java.math.BigDecimal average = reportService.getAverageDailyExpense(startDate, endDate);
            return ResponseEntity.ok(Map.of("averageDaily", average, "startDate", startDate, "endDate", endDate));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Compare expenses between two months
     * GET /api/reports/compare-months?year1=2025&month1=10&year2=2025&month2=9
     */
    @GetMapping("/compare-months")
    public ResponseEntity<Map<String, Object>> compareMonths(
            @RequestParam int year1,
            @RequestParam int month1,
            @RequestParam int year2,
            @RequestParam int month2) {
        try {
            if (month1 < 1 || month1 > 12 || month2 < 1 || month2 > 12) {
                return ResponseEntity.badRequest().build();
            }
            Map<String, Object> comparison = reportService.compareMonths(year1, month1, year2, month2);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get weekly spending trends for a date range
     * GET /api/reports/weekly-trends?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/weekly-trends")
    public ResponseEntity<Map<String, ?>> getWeeklyTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            Map<String, ?> trends = reportService.getWeeklyTrends(startDate, endDate);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
