package com.yourapp.expensetracker.expense_api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // ===== OPTIMIZED QUERIES WITH JOIN FETCH (Fixes N+1 Problem) =====
    
    /**
     * Find expenses by user with JOIN FETCH to avoid N+1 queries
     */
    @Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.user.id = :userId")
    List<Expense> findByUserIdWithUser(@Param("userId") Long userId);
    
    /**
     * Find expenses by user with pagination (no JOIN FETCH needed - DTO projection recommended)
     */
    Page<Expense> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find all expenses by user (legacy - prefer paginated version)
     */
    List<Expense> findByUserId(Long userId);
    
    // ===== USER-FILTERED QUERIES =====
    
    /**
     * Find expenses by user and category
     */
    @Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.user.id = :userId AND e.category = :category")
    List<Expense> findByUserIdAndCategoryWithUser(@Param("userId") Long userId, @Param("category") String category);
    
    Page<Expense> findByUserIdAndCategory(Long userId, String category, Pageable pageable);
    List<Expense> findByUserIdAndCategory(Long userId, String category);

    /**
     * Find expenses by user and date range
     */
    @Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    List<Expense> findByUserIdAndDateBetweenWithUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    Page<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Find expenses by user, category and date range
     */
    @Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.user.id = :userId AND e.category = :category AND e.date BETWEEN :startDate AND :endDate")
    List<Expense> findByUserIdAndCategoryAndDateBetweenWithUser(@Param("userId") Long userId, @Param("category") String category, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    Page<Expense> findByUserIdAndCategoryAndDateBetween(Long userId, String category, LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<Expense> findByUserIdAndCategoryAndDateBetween(Long userId, String category, LocalDate startDate, LocalDate endDate);

    /**
     * Find expenses by user and amount range
     */
    Page<Expense> findByUserIdAndAmountBetween(Long userId, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    List<Expense> findByUserIdAndAmountBetween(Long userId, BigDecimal minAmount, BigDecimal maxAmount);
    
    List<Expense> findByUserIdAndAmountGreaterThanEqual(Long userId, BigDecimal amount);
    List<Expense> findByUserIdAndAmountLessThanEqual(Long userId, BigDecimal amount);
    List<Expense> findByUserIdAndAmountGreaterThan(Long userId, BigDecimal amount);

    /**
     * Find expenses by user and description keyword
     */
    @Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.user.id = :userId AND LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Expense> findByUserIdAndDescriptionContainingIgnoreCaseWithUser(@Param("userId") Long userId, @Param("keyword") String keyword);
    
    Page<Expense> findByUserIdAndDescriptionContainingIgnoreCase(Long userId, String keyword, Pageable pageable);
    List<Expense> findByUserIdAndDescriptionContainingIgnoreCase(Long userId, String keyword);

    /**
     * Find all expenses by user ordered by date descending
     */
    @Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.user.id = :userId ORDER BY e.date DESC, e.createdAt DESC")
    List<Expense> findByUserIdOrderByDateDescWithUser(@Param("userId") Long userId);
    
    Page<Expense> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);

    /**
     * Find top N expenses by user ordered by amount descending
     */
    @Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.user.id = :userId ORDER BY e.amount DESC")
    List<Expense> findTop3ByUserIdOrderByAmountDescWithUser(@Param("userId") Long userId);
    
    List<Expense> findTop3ByUserIdOrderByAmountDesc(Long userId);
    
    // ===== LEGACY QUERIES (Deprecated - use user-filtered versions) =====
    
    /**
     * @deprecated Use findByUserIdAndDateBetween instead
     */
    @Deprecated
    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * @deprecated Use findByUserIdAndCategory instead
     */
    @Deprecated
    List<Expense> findByCategory(String category);
    
    /**
     * @deprecated Use findByUserIdAndCategoryAndDateBetween instead
     */
    @Deprecated
    List<Expense> findByCategoryAndDateBetween(String category, LocalDate startDate, LocalDate endDate);

    /**
     * @deprecated Use findByUserIdAndAmountGreaterThanEqual instead
     */
    @Deprecated
    List<Expense> findByAmountGreaterThanEqual(BigDecimal amount);

    /**
     * @deprecated Use findByUserIdAndAmountLessThanEqual instead
     */
    @Deprecated
    List<Expense> findByAmountLessThanEqual(BigDecimal amount);

    /**
     * @deprecated Use findByUserIdAndAmountGreaterThan instead
     */
    @Deprecated
    List<Expense> findByAmountGreaterThan(BigDecimal amount);

    /**
     * @deprecated Use findByUserIdAndAmountBetween instead
     */
    @Deprecated
    List<Expense> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * @deprecated Use findByUserIdAndDescriptionContainingIgnoreCase instead
     */
    @Deprecated
    List<Expense> findByDescriptionContainingIgnoreCase(String keyword);

    /**
     * @deprecated Use findByUserIdOrderByDateDesc instead
     */
    @Deprecated
    List<Expense> findAllByOrderByDateDesc();

    /**
     * @deprecated Use findTop3ByUserIdOrderByAmountDesc instead
     */
    @Deprecated
    List<Expense> findTop3ByOrderByAmountDesc();

    // ===== AGGREGATION QUERIES WITH USER FILTERING =====
    
    /**
     * Get sum of expenses by user, category and date range
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.category = :category AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndCategoryAndDateRange(@Param("userId") Long userId, 
                                                        @Param("category") String category, 
                                                        @Param("startDate") LocalDate startDate, 
                                                        @Param("endDate") LocalDate endDate);

    /**
     * Get total expenses by user within date range
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndDateRange(@Param("userId") Long userId, 
                                            @Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);

    /**
     * Get distinct categories for user
     */
    @Query("SELECT DISTINCT e.category FROM Expense e WHERE e.user.id = :userId ORDER BY e.category")
    List<String> findDistinctCategoriesByUserId(@Param("userId") Long userId);
    
    /**
     * Count expenses by user
     */
    long countByUserId(Long userId);
    
    /**
     * Count expenses by user and category
     */
    long countByUserIdAndCategory(Long userId, String category);
    
    /**
     * Count expenses by user and date range
     */
    long countByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    // ===== LEGACY AGGREGATION QUERIES (Deprecated) =====
    
    /**
     * @deprecated Use sumAmountByUserIdAndCategoryAndDateRange instead
     */
    @Deprecated
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.category = :category AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByCategoryAndDateRange(@Param("category") String category, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    /**
     * @deprecated Use sumAmountByUserIdAndDateRange instead
     */
    @Deprecated
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * @deprecated Use findDistinctCategoriesByUserId instead
     */
    @Deprecated
    @Query("SELECT DISTINCT e.category FROM Expense e ORDER BY e.category")
    List<String> findDistinctCategories();
}