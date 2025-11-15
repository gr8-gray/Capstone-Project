package com.yourapp.expensetracker.expense_api.service;

import com.yourapp.expensetracker.expense_api.dto.BudgetAlertDTO;
import com.yourapp.expensetracker.expense_api.model.Budget;
import com.yourapp.expensetracker.expense_api.model.BudgetAlert;
import com.yourapp.expensetracker.expense_api.model.BudgetAlert.AlertLevel;
import com.yourapp.expensetracker.expense_api.repository.BudgetAlertRepository;
import com.yourapp.expensetracker.expense_api.repository.BudgetRepository;
import com.yourapp.expensetracker.expense_api.repository.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing budget alerts and monitoring budget thresholds
 * @author Eric Gray - Backend Developer
 */
@Service
@Transactional
public class BudgetAlertService {

    private static final Logger logger = LoggerFactory.getLogger(BudgetAlertService.class);

    private final BudgetAlertRepository alertRepository;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    // Alert thresholds
    private static final BigDecimal THRESHOLD_INFO = new BigDecimal("50");      // 50%
    private static final BigDecimal THRESHOLD_WARNING = new BigDecimal("75");   // 75%
    private static final BigDecimal THRESHOLD_DANGER = new BigDecimal("90");    // 90%
    private static final BigDecimal THRESHOLD_CRITICAL = new BigDecimal("100"); // 100%

    public BudgetAlertService(BudgetAlertRepository alertRepository,
                              BudgetRepository budgetRepository,
                              ExpenseRepository expenseRepository) {
        this.alertRepository = alertRepository;
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    /**
     * Check all active budgets and generate alerts if thresholds are exceeded
     */
    public List<BudgetAlertDTO> checkAllBudgets() {
        logger.info("Starting budget alert check for all active budgets");
        
        LocalDate today = LocalDate.now();
        List<Budget> activeBudgets = budgetRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
        
        logger.info("Found {} active budgets to check", activeBudgets.size());
        
        List<BudgetAlert> alerts = activeBudgets.stream()
                .map(this::checkBudgetAndCreateAlert)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        
        logger.info("Generated {} new budget alerts", alerts.size());
        
        return alerts.stream()
                .map(BudgetAlertDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Check a specific budget and create alert if threshold exceeded
     */
    public Optional<BudgetAlertDTO> checkBudget(Long budgetId) {
        logger.info("Checking budget with ID: {}", budgetId);
        
        Optional<Budget> budgetOpt = budgetRepository.findById(budgetId);
        if (budgetOpt.isEmpty()) {
            logger.warn("Budget not found with ID: {}", budgetId);
            return Optional.empty();
        }
        
        Budget budget = budgetOpt.get();
        Optional<BudgetAlert> alertOpt = checkBudgetAndCreateAlert(budget);
        
        return alertOpt.map(BudgetAlertDTO::new);
    }

    /**
     * Internal method to check budget and create alert
     */
    private Optional<BudgetAlert> checkBudgetAndCreateAlert(Budget budget) {
        // Calculate total spent for this budget period and category
        BigDecimal totalSpent = expenseRepository
                .findByCategoryAndDateBetween(budget.getCategory(), budget.getStartDate(), budget.getEndDate())
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal budgetLimit = budget.getLimitAmount();
        
        // Calculate percentage used
        BigDecimal percentageUsed = totalSpent
                .divide(budgetLimit, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
        
        logger.debug("Budget '{}': Spent ${} of ${} ({}%)", 
                budget.getCategory(), totalSpent, budgetLimit, percentageUsed);
        
        // Determine alert level
        AlertLevel alertLevel = determineAlertLevel(percentageUsed);
        
        // Only create alert if threshold reached
        if (alertLevel != null) {
            String message = generateAlertMessage(budget, totalSpent, budgetLimit, percentageUsed, alertLevel);
            
            BudgetAlert alert = new BudgetAlert(
                    budget,
                    alertLevel,
                    message,
                    totalSpent,
                    budgetLimit,
                    percentageUsed
            );
            
            BudgetAlert savedAlert = alertRepository.save(alert);
            logger.warn("Budget alert created: {} - {}", alertLevel, message);
            
            return Optional.of(savedAlert);
        }
        
        return Optional.empty();
    }

    /**
     * Determine alert level based on percentage used
     */
    private AlertLevel determineAlertLevel(BigDecimal percentageUsed) {
        if (percentageUsed.compareTo(THRESHOLD_CRITICAL) >= 0) {
            return AlertLevel.CRITICAL;
        } else if (percentageUsed.compareTo(THRESHOLD_DANGER) >= 0) {
            return AlertLevel.DANGER;
        } else if (percentageUsed.compareTo(THRESHOLD_WARNING) >= 0) {
            return AlertLevel.WARNING;
        } else if (percentageUsed.compareTo(THRESHOLD_INFO) >= 0) {
            return AlertLevel.INFO;
        }
        return null; // Below threshold
    }

    /**
     * Generate human-readable alert message
     */
    private String generateAlertMessage(Budget budget, BigDecimal totalSpent, 
                                        BigDecimal budgetLimit, BigDecimal percentageUsed,
                                        AlertLevel alertLevel) {
        BigDecimal remaining = budgetLimit.subtract(totalSpent);
        
        switch (alertLevel) {
            case CRITICAL:
                if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                    return String.format("CRITICAL: '%s' budget exceeded by $%s! Spent: $%s of $%s (%.2f%%)",
                            budget.getCategory(), remaining.abs(), totalSpent, budgetLimit, percentageUsed);
                } else {
                    return String.format("CRITICAL: '%s' budget limit reached! Spent: $%s of $%s (%.2f%%)",
                            budget.getCategory(), totalSpent, budgetLimit, percentageUsed);
                }
            case DANGER:
                return String.format("DANGER: '%s' budget at %.2f%%! Only $%s remaining of $%s budget",
                        budget.getCategory(), percentageUsed, remaining, budgetLimit);
            case WARNING:
                return String.format("WARNING: '%s' budget at %.2f%%. Spent: $%s of $%s ($%s remaining)",
                        budget.getCategory(), percentageUsed, totalSpent, budgetLimit, remaining);
            case INFO:
                return String.format("INFO: '%s' budget halfway mark reached. Spent: $%s of $%s (%.2f%%)",
                        budget.getCategory(), totalSpent, budgetLimit, percentageUsed);
            default:
                return String.format("Budget status for '%s': $%s of $%s used",
                        budget.getCategory(), totalSpent, budgetLimit);
        }
    }

    /**
     * Get all unread alerts
     */
    public List<BudgetAlertDTO> getUnreadAlerts() {
        logger.info("Fetching all unread budget alerts");
        return alertRepository.findByIsReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(BudgetAlertDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get all critical unread alerts
     */
    public List<BudgetAlertDTO> getCriticalAlerts() {
        logger.info("Fetching critical unread budget alerts");
        return alertRepository.findCriticalUnreadAlerts()
                .stream()
                .map(BudgetAlertDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get all alerts for a specific budget
     */
    public List<BudgetAlertDTO> getAlertsByBudgetId(Long budgetId) {
        logger.info("Fetching alerts for budget ID: {}", budgetId);
        return alertRepository.findByBudgetId(budgetId)
                .stream()
                .map(BudgetAlertDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Mark an alert as read
     */
    public Optional<BudgetAlertDTO> markAlertAsRead(Long alertId) {
        logger.info("Marking alert as read: {}", alertId);
        
        Optional<BudgetAlert> alertOpt = alertRepository.findById(alertId);
        if (alertOpt.isEmpty()) {
            logger.warn("Alert not found with ID: {}", alertId);
            return Optional.empty();
        }
        
        BudgetAlert alert = alertOpt.get();
        alert.setIsRead(true);
        BudgetAlert updatedAlert = alertRepository.save(alert);
        
        logger.info("Alert {} marked as read", alertId);
        return Optional.of(new BudgetAlertDTO(updatedAlert));
    }

    /**
     * Mark all alerts as read
     */
    public int markAllAlertsAsRead() {
        logger.info("Marking all alerts as read");
        
        List<BudgetAlert> unreadAlerts = alertRepository.findByIsReadFalseOrderByCreatedAtDesc();
        unreadAlerts.forEach(alert -> alert.setIsRead(true));
        alertRepository.saveAll(unreadAlerts);
        
        logger.info("Marked {} alerts as read", unreadAlerts.size());
        return unreadAlerts.size();
    }

    /**
     * Get count of unread alerts
     */
    public Long getUnreadAlertCount() {
        Long count = alertRepository.countByIsReadFalse();
        logger.debug("Unread alert count: {}", count);
        return count;
    }

    /**
     * Delete old read alerts (cleanup)
     */
    public void deleteOldAlerts(int daysOld) {
        logger.info("Deleting alerts older than {} days", daysOld);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        alertRepository.deleteOldReadAlerts(cutoffDate);
        
        logger.info("Old alerts deleted successfully");
    }

    /**
     * Get all alerts
     */
    public List<BudgetAlertDTO> getAllAlerts() {
        logger.info("Fetching all budget alerts");
        return alertRepository.findAll()
                .stream()
                .map(BudgetAlertDTO::new)
                .collect(Collectors.toList());
    }
}
