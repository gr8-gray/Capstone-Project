package com.yourapp.expensetracker.expense_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourapp.expensetracker.expense_api.model.Expense;
import com.yourapp.expensetracker.expense_api.model.User;
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
    private final AuthService authService;

    @Autowired
    public ReportService(ExpenseRepository expenseRepository, AuthService authService) {
        this.expenseRepository = expenseRepository;
        this.authService = authService;
    }

    /**
     * Generate monthly expense summary (user-filtered)
     */
    public Map<String, Object> getMonthlyExpenseSummary(int year, int month) {
        User currentUser = authService.getCurrentUser();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenWithUser(
            currentUser.getId(), startDate, endDate);
        
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
     * Generate monthly report (alias for getMonthlyExpenseSummary) (user-filtered)
     * Used by integration tests
     */
    public Map<String, Object> getMonthlyReport(int year, int month) {
        User currentUser = authService.getCurrentUser();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenWithUser(
            currentUser.getId(), startDate, endDate);
        
        Map<String, Object> report = new HashMap<>();
        report.put("month", month);
        report.put("year", year);
        report.put("expenseCount", expenses.size());
        report.put("totalExpenses", getTotalAmount(expenses));
        report.put("expenses", expenses);
        report.put("categoryBreakdown", getCategoryBreakdown(expenses));
        
        return report;
    }

    /**
     * Generate yearly report (user-filtered)
     */
    public Map<String, Object> getYearlyReport(int year) {
        User currentUser = authService.getCurrentUser();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenWithUser(
            currentUser.getId(), startDate, endDate);
        
        Map<String, Object> report = new HashMap<>();
        report.put("year", year);
        report.put("totalExpenses", getTotalAmount(expenses));
        report.put("expenseCount", expenses.size());
        report.put("monthlyBreakdown", getMonthlyTrends(expenses));
        report.put("categoryBreakdown", getCategoryBreakdown(expenses));
        report.put("averageMonthly", getAverageMonthly(expenses));
        
        return report;
    }

    /**
     * Get category breakdown for date range (user-filtered)
     * Overloaded version that takes date parameters
     */
    public Map<String, BigDecimal> getCategoryBreakdown(LocalDate startDate, LocalDate endDate) {
        User currentUser = authService.getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenWithUser(
            currentUser.getId(), startDate, endDate);
        return getCategoryBreakdown(expenses);
    }

    /**
     * Get total expenses for date range (user-filtered)
     */
    public BigDecimal getTotalForDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = authService.getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenWithUser(
            currentUser.getId(), startDate, endDate);
        return getTotalAmount(expenses);
    }

    /**
     * Get top expense categories for date range (user-filtered)
     */
    public Map<String, BigDecimal> getTopExpenseCategories(LocalDate startDate, LocalDate endDate, int limit) {
        User currentUser = authService.getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenWithUser(
            currentUser.getId(), startDate, endDate);
        Map<String, BigDecimal> categoryTotals = getCategoryBreakdown(expenses);
        
        return categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
    }

    /**
     * Get average daily expense for date range (user-filtered)
     */
    public BigDecimal getAverageDailyExpense(LocalDate startDate, LocalDate endDate) {
        User currentUser = authService.getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenWithUser(
            currentUser.getId(), startDate, endDate);
        return getAverageDaily(expenses, startDate, endDate);
    }

    /**
     * Compare expenses between two months (user-filtered)
     */
    public Map<String, Object> compareMonths(int year1, int month1, int year2, int month2) {
        User currentUser = authService.getCurrentUser();
        
        // Get first month data
        YearMonth yearMonth1 = YearMonth.of(year1, month1);
        LocalDate start1 = yearMonth1.atDay(1);
        LocalDate end1 = yearMonth1.atEndOfMonth();
        List<Expense> expenses1 = expenseRepository.findByUserIdAndDateBetweenWithUser(
            currentUser.getId(), start1, end1);
        BigDecimal total1 = getTotalAmount(expenses1);
        
        // Get second month data
        YearMonth yearMonth2 = YearMonth.of(year2, month2);
        LocalDate start2 = yearMonth2.atDay(1);
        LocalDate end2 = yearMonth2.atEndOfMonth();
        List<Expense> expenses2 = expenseRepository.findByUserIdAndDateBetweenWithUser(
            currentUser.getId(), start2, end2);
        BigDecimal total2 = getTotalAmount(expenses2);
        
        // Calculate comparison
        BigDecimal difference = total2.subtract(total1);
        double percentChange = calculatePercentageChange(total1, total2);
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("month1", month1);
        comparison.put("year1", year1);
        comparison.put("month1Total", total1);
        comparison.put("month1Count", expenses1.size());
        comparison.put("month2", month2);
        comparison.put("year2", year2);
        comparison.put("month2Total", total2);
        comparison.put("month2Count", expenses2.size());
        comparison.put("difference", difference);
        comparison.put("percentChange", percentChange);
        
        return comparison;
    }

    /**
     * Get category report with specified category filter (user-filtered)
     * Overloaded version for specific category
     */
    public Map<String, Object> getCategoryReport(String category, LocalDate startDate, LocalDate endDate) {
        User currentUser = authService.getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserIdAndCategoryAndDateBetweenWithUser(
            currentUser.getId(), category, startDate, endDate);
        
        Map<String, Object> report = new HashMap<>();
        report.put("category", category);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("total", getTotalAmount(expenses));
        report.put("expenseCount", expenses.size());
        report.put("expenses", expenses);
        report.put("averageExpense", getAverageExpenseAmount(expenses));
        
        return report;
    }

    /**
     * Get weekly trends for date range
     * Overloaded version that takes date parameters
     */
    public Map<String, BigDecimal> getWeeklyTrends(LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByDateBetween(startDate, endDate);
        return getWeeklyTrends(expenses);
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