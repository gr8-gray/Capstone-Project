package com.yourapp.expensetracker.expense_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.yourapp.expensetracker.expense_api.model.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Expense entity operations
 * Extends JpaRepository to get basic CRUD methods for free
 * @author Eric Gray - Backend Developer
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Custom query methods using Spring Data JPA naming conventions
    List<Expense> findByUserId(Long userId);
    /**
     * Find expenses by category
     */
    List<Expense> findByCategory(String category);

    /**
     * Find expenses by date range
     */
    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find expenses by category and date range
     */
    List<Expense> findByCategoryAndDateBetween(String category, LocalDate startDate, LocalDate endDate);

    /**
     * Find expenses greater than or equal to amount
     */
    List<Expense> findByAmountGreaterThanEqual(BigDecimal amount);

    /**
     * Find expenses less than or equal to amount
     */
    List<Expense> findByAmountLessThanEqual(BigDecimal amount);

    /**
     * Find expenses greater than amount
     */
    List<Expense> findByAmountGreaterThan(BigDecimal amount);

    /**
     * Find expenses between min and max amount
     */
    List<Expense> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find expenses by description containing keyword (case-insensitive)
     */
    List<Expense> findByDescriptionContainingIgnoreCase(String keyword);

    /**
     * Find all expenses ordered by date descending
     */
    List<Expense> findAllByOrderByDateDesc();

    /**
     * Find top N expenses ordered by amount descending
     */
    List<Expense> findTop3ByOrderByAmountDesc();

    /**
     * Get sum of expenses by category within date range
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.category = :category AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByCategoryAndDateRange(@Param("category") String category, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    /**
     * Get total expenses within date range
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Get distinct categories
     */
    @Query("SELECT DISTINCT e.category FROM Expense e ORDER BY e.category")
    List<String> findDistinctCategories();

    // TODO: Add user-specific queries when User entity is implemented
    // List<Expense> findByUserId(Long userId);
    // List<Expense> findByUserIdAndCategory(Long userId, String category);
    // List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}