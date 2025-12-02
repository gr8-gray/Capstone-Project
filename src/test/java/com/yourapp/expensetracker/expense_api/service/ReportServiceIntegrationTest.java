package com.yourapp.expensetracker.expense_api.service;

import com.yourapp.expensetracker.expense_api.BaseIntegrationTest;
import com.yourapp.expensetracker.expense_api.model.Expense;
import com.yourapp.expensetracker.expense_api.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ReportService
 * Tests report generation and analytics functionality
 * 
 * @author Eric Gray - Backend Developer
 */
class ReportServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        super.setUpBaseTest();
        expenseRepository.deleteAll();
    }

    @Test
    void shouldGenerateMonthlyReport() {
        // Given - Create expenses for current month
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        // Ensure dates are within current month
        LocalDate date1 = currentMonth.atDay(15);  // Mid-month
        LocalDate date2 = currentMonth.atDay(10);  // Earlier in month
        LocalDate date3 = currentMonth.atDay(20);  // Later in month

        Expense expense1 = new Expense();
        expense1.setDescription("Grocery 1");
        expense1.setAmount(new BigDecimal("50.00"));
        expense1.setCategory("Food");
        expense1.setDate(date1);
        expense1.setUser(testUser);
        expense1.setCreatedAt(LocalDateTime.now());
        expense1.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Grocery 2");
        expense2.setAmount(new BigDecimal("75.00"));
        expense2.setCategory("Food");
        expense2.setDate(date2);
        expense2.setUser(testUser);
        expense2.setCreatedAt(LocalDateTime.now());
        expense2.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense2);

        Expense expense3 = new Expense();
        expense3.setDescription("Gas");
        expense3.setAmount(new BigDecimal("40.00"));
        expense3.setCategory("Transportation");
        expense3.setDate(date3);
        expense3.setUser(testUser);
        expense3.setCreatedAt(LocalDateTime.now());
        expense3.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense3);

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
        
        Expense expense1 = new Expense();
        expense1.setDescription("Lunch");
        expense1.setAmount(new BigDecimal("15.00"));
        expense1.setCategory("Food");
        expense1.setDate(today);
        expense1.setUser(testUser);
        expense1.setCreatedAt(LocalDateTime.now());
        expense1.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Dinner");
        expense2.setAmount(new BigDecimal("35.00"));
        expense2.setCategory("Food");
        expense2.setDate(today);
        expense2.setUser(testUser);
        expense2.setCreatedAt(LocalDateTime.now());
        expense2.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense2);

        Expense expense3 = new Expense();
        expense3.setDescription("Gas");
        expense3.setAmount(new BigDecimal("50.00"));
        expense3.setCategory("Transportation");
        expense3.setDate(today);
        expense3.setUser(testUser);
        expense3.setCreatedAt(LocalDateTime.now());
        expense3.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense3);

        Expense expense4 = new Expense();
        expense4.setDescription("Movie");
        expense4.setAmount(new BigDecimal("20.00"));
        expense4.setCategory("Entertainment");
        expense4.setDate(today);
        expense4.setUser(testUser);
        expense4.setCreatedAt(LocalDateTime.now());
        expense4.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense4);

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
        
        Expense expense1 = new Expense();
        expense1.setDescription("In range 1");
        expense1.setAmount(new BigDecimal("25.00"));
        expense1.setCategory("Food");
        expense1.setDate(startDate);
        expense1.setUser(testUser);
        expense1.setCreatedAt(LocalDateTime.now());
        expense1.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("In range 2");
        expense2.setAmount(new BigDecimal("30.00"));
        expense2.setCategory("Food");
        expense2.setDate(startDate.plusDays(3));
        expense2.setUser(testUser);
        expense2.setCreatedAt(LocalDateTime.now());
        expense2.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense2);

        Expense expense3 = new Expense();
        expense3.setDescription("In range 3");
        expense3.setAmount(new BigDecimal("45.00"));
        expense3.setCategory("Food");
        expense3.setDate(endDate);
        expense3.setUser(testUser);
        expense3.setCreatedAt(LocalDateTime.now());
        expense3.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense3);

        Expense expense4 = new Expense();
        expense4.setDescription("Out of range");
        expense4.setAmount(new BigDecimal("100.00"));
        expense4.setCategory("Food");
        expense4.setDate(startDate.minusDays(1));
        expense4.setUser(testUser);
        expense4.setCreatedAt(LocalDateTime.now());
        expense4.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense4);

        // When
        BigDecimal total = reportService.getTotalForDateRange(startDate, endDate);

        // Then
        assertThat(total).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldGenerateYearlyReport() {
        // Given - Create expenses throughout the year
        int currentYear = LocalDate.now().getYear();
        
        Expense expense1 = new Expense();
        expense1.setDescription("Jan expense");
        expense1.setAmount(new BigDecimal("100.00"));
        expense1.setCategory("Food");
        expense1.setDate(LocalDate.of(currentYear, 1, 15));
        expense1.setUser(testUser);
        expense1.setCreatedAt(LocalDateTime.now());
        expense1.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Feb expense");
        expense2.setAmount(new BigDecimal("150.00"));
        expense2.setCategory("Food");
        expense2.setDate(LocalDate.of(currentYear, 2, 15));
        expense2.setUser(testUser);
        expense2.setCreatedAt(LocalDateTime.now());
        expense2.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense2);

        Expense expense3 = new Expense();
        expense3.setDescription("Mar expense");
        expense3.setAmount(new BigDecimal("200.00"));
        expense3.setCategory("Food");
        expense3.setDate(LocalDate.of(currentYear, 3, 15));
        expense3.setUser(testUser);
        expense3.setCreatedAt(LocalDateTime.now());
        expense3.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense3);

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
        
        Expense expense1 = new Expense();
        expense1.setDescription("Food 1");
        expense1.setAmount(new BigDecimal("100.00"));
        expense1.setCategory("Food");
        expense1.setDate(today);
        expense1.setUser(testUser);
        expense1.setCreatedAt(LocalDateTime.now());
        expense1.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Food 2");
        expense2.setAmount(new BigDecimal("150.00"));
        expense2.setCategory("Food");
        expense2.setDate(today);
        expense2.setUser(testUser);
        expense2.setCreatedAt(LocalDateTime.now());
        expense2.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense2);

        Expense expense3 = new Expense();
        expense3.setDescription("Transport");
        expense3.setAmount(new BigDecimal("80.00"));
        expense3.setCategory("Transportation");
        expense3.setDate(today);
        expense3.setUser(testUser);
        expense3.setCreatedAt(LocalDateTime.now());
        expense3.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense3);

        Expense expense4 = new Expense();
        expense4.setDescription("Entertainment");
        expense4.setAmount(new BigDecimal("50.00"));
        expense4.setCategory("Entertainment");
        expense4.setDate(today);
        expense4.setUser(testUser);
        expense4.setCreatedAt(LocalDateTime.now());
        expense4.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense4);

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
        Expense expense1 = new Expense();
        expense1.setDescription("Day 1");
        expense1.setAmount(new BigDecimal("20.00"));
        expense1.setCategory("Food");
        expense1.setDate(startDate);
        expense1.setUser(testUser);
        expense1.setCreatedAt(LocalDateTime.now());
        expense1.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Day 2");
        expense2.setAmount(new BigDecimal("30.00"));
        expense2.setCategory("Food");
        expense2.setDate(startDate.plusDays(1));
        expense2.setUser(testUser);
        expense2.setCreatedAt(LocalDateTime.now());
        expense2.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense2);

        Expense expense3 = new Expense();
        expense3.setDescription("Day 3");
        expense3.setAmount(new BigDecimal("25.00"));
        expense3.setCategory("Food");
        expense3.setDate(startDate.plusDays(2));
        expense3.setUser(testUser);
        expense3.setCreatedAt(LocalDateTime.now());
        expense3.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense3);

        Expense expense4 = new Expense();
        expense4.setDescription("Day 4");
        expense4.setAmount(new BigDecimal("25.00"));
        expense4.setCategory("Food");
        expense4.setDate(endDate);
        expense4.setUser(testUser);
        expense4.setCreatedAt(LocalDateTime.now());
        expense4.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense4);

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
        Expense expense1 = new Expense();
        expense1.setDescription("Jan 1");
        expense1.setAmount(new BigDecimal("50.00"));
        expense1.setCategory("Food");
        expense1.setDate(LocalDate.of(currentYear, 1, 15));
        expense1.setUser(testUser);
        expense1.setCreatedAt(LocalDateTime.now());
        expense1.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Jan 2");
        expense2.setAmount(new BigDecimal("50.00"));
        expense2.setCategory("Food");
        expense2.setDate(LocalDate.of(currentYear, 1, 20));
        expense2.setUser(testUser);
        expense2.setCreatedAt(LocalDateTime.now());
        expense2.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense2);
        
        // February expenses
        Expense expense3 = new Expense();
        expense3.setDescription("Feb 1");
        expense3.setAmount(new BigDecimal("75.00"));
        expense3.setCategory("Food");
        expense3.setDate(LocalDate.of(currentYear, 2, 15));
        expense3.setUser(testUser);
        expense3.setCreatedAt(LocalDateTime.now());
        expense3.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense3);

        Expense expense4 = new Expense();
        expense4.setDescription("Feb 2");
        expense4.setAmount(new BigDecimal("75.00"));
        expense4.setCategory("Food");
        expense4.setDate(LocalDate.of(currentYear, 2, 20));
        expense4.setUser(testUser);
        expense4.setCreatedAt(LocalDateTime.now());
        expense4.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense4);

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
        
        Expense expense1 = new Expense();
        expense1.setDescription("Food expense");
        expense1.setAmount(new BigDecimal("30.00"));
        expense1.setCategory("Food");
        expense1.setDate(today);
        expense1.setUser(testUser);
        expense1.setCreatedAt(LocalDateTime.now());
        expense1.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Transport expense");
        expense2.setAmount(new BigDecimal("50.00"));
        expense2.setCategory("Transportation");
        expense2.setDate(today);
        expense2.setUser(testUser);
        expense2.setCreatedAt(LocalDateTime.now());
        expense2.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense2);

        Expense expense3 = new Expense();
        expense3.setDescription("Food expense 2");
        expense3.setAmount(new BigDecimal("20.00"));
        expense3.setCategory("Food");
        expense3.setDate(today);
        expense3.setUser(testUser);
        expense3.setCreatedAt(LocalDateTime.now());
        expense3.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense3);

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
        Expense expense1 = new Expense();
        expense1.setDescription("Week 1-1");
        expense1.setAmount(new BigDecimal("50.00"));
        expense1.setCategory("Food");
        expense1.setDate(twoWeeksAgo);
        expense1.setUser(testUser);
        expense1.setCreatedAt(LocalDateTime.now());
        expense1.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Week 1-2");
        expense2.setAmount(new BigDecimal("50.00"));
        expense2.setCategory("Food");
        expense2.setDate(twoWeeksAgo.plusDays(3));
        expense2.setUser(testUser);
        expense2.setCreatedAt(LocalDateTime.now());
        expense2.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense2);
        
        // Week 2
        Expense expense3 = new Expense();
        expense3.setDescription("Week 2-1");
        expense3.setAmount(new BigDecimal("75.00"));
        expense3.setCategory("Food");
        expense3.setDate(twoWeeksAgo.plusDays(7));
        expense3.setUser(testUser);
        expense3.setCreatedAt(LocalDateTime.now());
        expense3.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense3);

        Expense expense4 = new Expense();
        expense4.setDescription("Week 2-2");
        expense4.setAmount(new BigDecimal("75.00"));
        expense4.setCategory("Food");
        expense4.setDate(twoWeeksAgo.plusDays(10));
        expense4.setUser(testUser);
        expense4.setCreatedAt(LocalDateTime.now());
        expense4.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense4);

        // When
        Map<String, BigDecimal> weeklyTrends = reportService.getWeeklyTrends(twoWeeksAgo, today);

        // Then
        assertThat(weeklyTrends).isNotEmpty();
    }
}
