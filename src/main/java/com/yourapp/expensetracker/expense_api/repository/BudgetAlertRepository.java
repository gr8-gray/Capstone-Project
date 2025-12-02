package com.yourapp.expensetracker.expense_api.repository;

import com.yourapp.expensetracker.expense_api.model.BudgetAlert;
import com.yourapp.expensetracker.expense_api.model.BudgetAlert.AlertLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for BudgetAlert entity operations
 * @author Eric Gray - Backend Developer
 */
@Repository
public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {

    /**
     * Find all unread alerts
     */
    List<BudgetAlert> findByIsReadFalseOrderByCreatedAtDesc();

    /**
     * Find alerts by budget ID
     */
    @Query("SELECT ba FROM BudgetAlert ba WHERE ba.budget.id = :budgetId ORDER BY ba.createdAt DESC")
    List<BudgetAlert> findByBudgetId(@Param("budgetId") Long budgetId);

    /**
     * Find alerts by alert level
     */
    List<BudgetAlert> findByAlertLevelOrderByCreatedAtDesc(AlertLevel alertLevel);

    /**
     * Find critical unread alerts
     */
    @Query("SELECT ba FROM BudgetAlert ba WHERE ba.isRead = false AND ba.alertLevel IN ('DANGER', 'CRITICAL') ORDER BY ba.createdAt DESC")
    List<BudgetAlert> findCriticalUnreadAlerts();

    /**
     * Count unread alerts
     */
    Long countByIsReadFalse();

    /**
     * Find alerts created after a specific date
     */
    List<BudgetAlert> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime dateTime);

    /**
     * Delete old read alerts
     */
    @Query("DELETE FROM BudgetAlert ba WHERE ba.isRead = true AND ba.readAt < :cutoffDate")
    void deleteOldReadAlerts(@Param("cutoffDate") LocalDateTime cutoffDate);
}
