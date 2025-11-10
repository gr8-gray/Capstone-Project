package com.yourapp.expensetracker.expense_api.service;

import com.yourapp.expensetracker.expense_api.model.Expense;
import com.yourapp.expensetracker.expense_api.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ReportService
 * Tests report generation and analytics functionality
 * 
 * @author Eric Gray - Backend Developer
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ReportServiceIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
    }

    @Test
    void shouldGenerateMonthlyReport() {
        // Given - Create expenses for current month
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        expenseRepository.save(new Expense("Grocery 1", new BigDecimal("50.00"), "Food", today));
        expenseRepository.save(new Expense("Grocery 2", new BigDecimal("75.00"), "Food", today.minusDays(5)));
        expenseRepository.save(new Expense("Gas", new BigDecimal("40.00"), "Transportation", today.minusDays(3)));

        // When
        Map<String, Object> report = reportService.getMonthlyReport(
            currentMonth.getYear(),
            currentMonth.getMonthValue()
        );

        // Then
        assertThat(report).isNotNull();
        assertThat(report).containsKeys("month", "year", "totalExpenses", "expenseCount");
        assertThat((BigDecimal) report.get("totalExpenses"))
            .isGreaterThanOrEqualTo(new BigDecimal("165.00"));
    }

    @Test
    void shouldGenerateCategoryBreakdown() {
        // Given
        LocalDate today = LocalDate.now();
        expenseRepository.save(new Expense("Lunch", new BigDecimal("15.00"), "Food", today));
        expenseRepository.save(new Expense("Dinner", new BigDecimal("35.00"), "Food", today));
        expenseRepository.save(new Expense("Gas", new BigDecimal("50.00"), "Transportation", today));
        expenseRepository.save(new Expense("Movie", new BigDecimal("20.00"), "Entertainment", today));

        // When
        Map<String, BigDecimal> categoryBreakdown = reportService.getCategoryBreakdown(
            today.minusDays(1),
            today.plusDays(1)
        );

        // Then
        assertThat(categoryBreakdown).isNotNull();
        assertThat(categoryBreakdown).containsKeys("Food", "Transportation", "Entertainment");
        assertThat(categoryBreakdown.get("Food")).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(categoryBreakdown.get("Transportation")).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(categoryBreakdown.get("Entertainment")).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    void shouldCalculateTotalForDateRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        expenseRepository.save(new Expense("In range 1", new BigDecimal("25.00"), "Food", startDate));
        expenseRepository.save(new Expense("In range 2", new BigDecimal("30.00"), "Food", startDate.plusDays(3)));
        expenseRepository.save(new Expense("In range 3", new BigDecimal("45.00"), "Food", endDate));
        expenseRepository.save(new Expense("Out of range", new BigDecimal("100.00"), "Food", startDate.minusDays(1)));

        // When
        BigDecimal total = reportService.getTotalForDateRange(startDate, endDate);

        // Then
        assertThat(total).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldGenerateYearlyReport() {
        // Given - Create expenses throughout the year
        int currentYear = LocalDate.now().getYear();
        
        expenseRepository.save(new Expense("Jan expense", new BigDecimal("100.00"), "Food", 
            LocalDate.of(currentYear, 1, 15)));
        expenseRepository.save(new Expense("Feb expense", new BigDecimal("150.00"), "Food", 
            LocalDate.of(currentYear, 2, 15)));
        expenseRepository.save(new Expense("Mar expense", new BigDecimal("200.00"), "Food", 
            LocalDate.of(currentYear, 3, 15)));

        // When
        Map<String, Object> yearlyReport = reportService.getYearlyReport(currentYear);

        // Then
        assertThat(yearlyReport).isNotNull();
        assertThat(yearlyReport).containsKey("year");
        assertThat(yearlyReport).containsKey("totalExpenses");
        assertThat((BigDecimal) yearlyReport.get("totalExpenses"))
            .isGreaterThanOrEqualTo(new BigDecimal("450.00"));
    }

    @Test
    void shouldIdentifyTopExpenseCategories() {
        // Given
        LocalDate today = LocalDate.now();
        expenseRepository.save(new Expense("Food 1", new BigDecimal("100.00"), "Food", today));
        expenseRepository.save(new Expense("Food 2", new BigDecimal("150.00"), "Food", today));
        expenseRepository.save(new Expense("Transport", new BigDecimal("80.00"), "Transportation", today));
        expenseRepository.save(new Expense("Entertainment", new BigDecimal("50.00"), "Entertainment", today));

        // When
        Map<String, BigDecimal> topCategories = reportService.getTopExpenseCategories(
            today.minusDays(1),
            today.plusDays(1),
            2
        );

        // Then
        assertThat(topCategories).hasSize(2);
        assertThat(topCategories).containsKey("Food");
        assertThat(topCategories.get("Food")).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    void shouldCalculateAverageDailyExpense() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(3);
        LocalDate endDate = LocalDate.now();
        
        // Total: 100.00 over 4 days = 25.00 per day average
        expenseRepository.save(new Expense("Day 1", new BigDecimal("20.00"), "Food", startDate));
        expenseRepository.save(new Expense("Day 2", new BigDecimal("30.00"), "Food", startDate.plusDays(1)));
        expenseRepository.save(new Expense("Day 3", new BigDecimal("25.00"), "Food", startDate.plusDays(2)));
        expenseRepository.save(new Expense("Day 4", new BigDecimal("25.00"), "Food", endDate));

        // When
        BigDecimal averageDaily = reportService.getAverageDailyExpense(startDate, endDate);

        // Then
        assertThat(averageDaily).isNotNull();
        assertThat(averageDaily).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    void shouldCompareMonthOverMonth() {
        // Given
        int currentYear = LocalDate.now().getYear();
        
        // January expenses
        expenseRepository.save(new Expense("Jan 1", new BigDecimal("50.00"), "Food", 
            LocalDate.of(currentYear, 1, 15)));
        expenseRepository.save(new Expense("Jan 2", new BigDecimal("50.00"), "Food", 
            LocalDate.of(currentYear, 1, 20)));
        
        // February expenses
        expenseRepository.save(new Expense("Feb 1", new BigDecimal("75.00"), "Food", 
            LocalDate.of(currentYear, 2, 15)));
        expenseRepository.save(new Expense("Feb 2", new BigDecimal("75.00"), "Food", 
            LocalDate.of(currentYear, 2, 20)));

        // When
        Map<String, Object> comparison = reportService.compareMonths(
            currentYear, 1,  // January
            currentYear, 2   // February
        );

        // Then
        assertThat(comparison).isNotNull();
        assertThat(comparison).containsKeys("month1Total", "month2Total", "difference", "percentChange");
    }

    @Test
    void shouldHandleEmptyReports() {
        // Given - No expenses
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        // When
        Map<String, Object> report = reportService.getMonthlyReport(
            currentMonth.getYear(),
            currentMonth.getMonthValue()
        );

        // Then
        assertThat(report).isNotNull();
        assertThat((Integer) report.get("expenseCount")).isEqualTo(0);
        assertThat((BigDecimal) report.get("totalExpenses")).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldGenerateReportWithFilters() {
        // Given
        LocalDate today = LocalDate.now();
        expenseRepository.save(new Expense("Food expense", new BigDecimal("30.00"), "Food", today));
        expenseRepository.save(new Expense("Transport expense", new BigDecimal("50.00"), "Transportation", today));
        expenseRepository.save(new Expense("Food expense 2", new BigDecimal("20.00"), "Food", today));

        // When - Get report filtered by Food category
        Map<String, Object> foodReport = reportService.getCategoryReport(
            "Food",
            today.minusDays(1),
            today.plusDays(1)
        );

        // Then
        assertThat(foodReport).isNotNull();
        assertThat(foodReport).containsKey("category");
        assertThat(foodReport.get("category")).isEqualTo("Food");
        assertThat((BigDecimal) foodReport.get("total")).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void shouldCalculateWeeklyTrends() {
        // Given - Create expenses over 2 weeks
        LocalDate today = LocalDate.now();
        LocalDate twoWeeksAgo = today.minusDays(14);
        
        // Week 1
        expenseRepository.save(new Expense("Week 1-1", new BigDecimal("50.00"), "Food", twoWeeksAgo));
        expenseRepository.save(new Expense("Week 1-2", new BigDecimal("50.00"), "Food", twoWeeksAgo.plusDays(3)));
        
        // Week 2
        expenseRepository.save(new Expense("Week 2-1", new BigDecimal("75.00"), "Food", twoWeeksAgo.plusDays(7)));
        expenseRepository.save(new Expense("Week 2-2", new BigDecimal("75.00"), "Food", twoWeeksAgo.plusDays(10)));

        // When
        Map<String, BigDecimal> weeklyTrends = reportService.getWeeklyTrends(twoWeeksAgo, today);

        // Then
        assertThat(weeklyTrends).isNotEmpty();
    }
}
