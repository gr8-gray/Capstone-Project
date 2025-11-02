package com.yourapp.expensetracker.expense_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourapp.expensetracker.expense_api.model.Expense;
import com.yourapp.expensetracker.expense_api.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating expense reports and analytics
 * Provides data aggregation and statistical analysis
 * @author Eric Gray - Backend Developer
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ReportService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    /**
     * Generate monthly expense summary
     */
    public Map<String, Object> getMonthlyExpenseSummary(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<Expense> expenses = expenseRepository.findByDateBetween(startDate, endDate);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("month", month);
        summary.put("year", year);
        summary.put("totalExpenses", expenses.size());
        summary.put("totalAmount", getTotalAmount(expenses));
        summary.put("categoryBreakdown", getCategoryBreakdown(expenses));
        summary.put("dailyTotals", getDailyTotals(expenses));
        summary.put("averageDaily", getAverageDaily(expenses, startDate, endDate));
        
        return summary;
    }

    /**
     * Generate category-wise expense report
     */
    public Map<String, Object> getCategoryReport(LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByDateBetween(startDate, endDate);
        Map<String, BigDecimal> categoryTotals = getCategoryBreakdown(expenses);
        
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("categoryTotals", categoryTotals);
        report.put("totalAmount", getTotalAmount(expenses));
        report.put("categoryPercentages", getCategoryPercentages(categoryTotals));
        report.put("topCategories", getTopCategories(categoryTotals, 5));
        
        return report;
    }

    /**
     * Generate expense trends over time
     */
    public Map<String, Object> getExpenseTrends(LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByDateBetween(startDate, endDate);
        
        Map<String, Object> trends = new HashMap<>();
        trends.put("startDate", startDate);
        trends.put("endDate", endDate);
        trends.put("monthlyTrends", getMonthlyTrends(expenses));
        trends.put("weeklyTrends", getWeeklyTrends(expenses));
        trends.put("totalAmount", getTotalAmount(expenses));
        trends.put("averageMonthly", getAverageMonthly(expenses));
        
        return trends;
    }

    /**
     * Get spending comparison between periods
     */
    public Map<String, Object> getSpendingComparison(LocalDate currentStart, LocalDate currentEnd, 
                                                   LocalDate previousStart, LocalDate previousEnd) {
        List<Expense> currentExpenses = expenseRepository.findByDateBetween(currentStart, currentEnd);
        List<Expense> previousExpenses = expenseRepository.findByDateBetween(previousStart, previousEnd);
        
        BigDecimal currentTotal = getTotalAmount(currentExpenses);
        BigDecimal previousTotal = getTotalAmount(previousExpenses);
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("currentPeriod", Map.of(
            "startDate", currentStart,
            "endDate", currentEnd,
            "totalAmount", currentTotal,
            "expenseCount", currentExpenses.size()
        ));
        comparison.put("previousPeriod", Map.of(
            "startDate", previousStart,
            "endDate", previousEnd,
            "totalAmount", previousTotal,
            "expenseCount", previousExpenses.size()
        ));
        comparison.put("change", currentTotal.subtract(previousTotal));
        comparison.put("percentageChange", calculatePercentageChange(previousTotal, currentTotal));
        
        return comparison;
    }

    /**
     * Get expenses above average
     */
    public List<Expense> getExpensesAboveAverage(LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByDateBetween(startDate, endDate);
        BigDecimal average = getAverageExpenseAmount(expenses);
        
        return expenses.stream()
                .filter(expense -> expense.getAmount().compareTo(average) > 0)
                .sorted((e1, e2) -> e2.getAmount().compareTo(e1.getAmount()))
                .collect(Collectors.toList());
    }

    // Helper methods

    private BigDecimal getTotalAmount(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> getCategoryBreakdown(List<Expense> expenses) {
        return expenses.stream()
                .collect(Collectors.groupingBy(
                    Expense::getCategory,
                    Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
    }

    private Map<LocalDate, BigDecimal> getDailyTotals(List<Expense> expenses) {
        return expenses.stream()
                .collect(Collectors.groupingBy(
                    Expense::getDate,
                    Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
    }

    private BigDecimal getAverageDaily(List<Expense> expenses, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = getTotalAmount(expenses);
        long daysBetween = startDate.datesUntil(endDate.plusDays(1)).count();
        return daysBetween > 0 ? total.divide(BigDecimal.valueOf(daysBetween), 2, RoundingMode.HALF_UP) 
                               : BigDecimal.ZERO;
    }

    private Map<String, Double> getCategoryPercentages(Map<String, BigDecimal> categoryTotals) {
        BigDecimal total = categoryTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return new HashMap<>();
        }
        
        return categoryTotals.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().divide(total, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                ));
    }

    private List<Map.Entry<String, BigDecimal>> getTopCategories(Map<String, BigDecimal> categoryTotals, int limit) {
        return categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Map<YearMonth, BigDecimal> getMonthlyTrends(List<Expense> expenses) {
        return expenses.stream()
                .collect(Collectors.groupingBy(
                    expense -> YearMonth.from(expense.getDate()),
                    Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
    }

    private Map<String, BigDecimal> getWeeklyTrends(List<Expense> expenses) {
        // Simplified weekly grouping - you can enhance this based on requirements
        return expenses.stream()
                .collect(Collectors.groupingBy(
                    expense -> "Week of " + expense.getDate().minusDays(expense.getDate().getDayOfWeek().getValue() - 1),
                    Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
    }

    private BigDecimal getAverageMonthly(List<Expense> expenses) {
        Map<YearMonth, BigDecimal> monthlyTotals = getMonthlyTrends(expenses);
        if (monthlyTotals.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = monthlyTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(monthlyTotals.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getAverageExpenseAmount(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = getTotalAmount(expenses);
        return total.divide(BigDecimal.valueOf(expenses.size()), 2, RoundingMode.HALF_UP);
    }

    private double calculatePercentageChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return newValue.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 100.0;
        }
        
        BigDecimal change = newValue.subtract(oldValue);
        return change.divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}