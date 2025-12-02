package com.yourapp.expensetracker.expense_api.controller;

import com.yourapp.expensetracker.expense_api.dto.BudgetAlertDTO;
import com.yourapp.expensetracker.expense_api.service.BudgetAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Budget Alert operations
 * @author Eric Gray - Backend Developer
 */
@RestController
@RequestMapping("/api/budget-alerts")
@CrossOrigin(origins = "*")
public class BudgetAlertController {

    private static final Logger logger = LoggerFactory.getLogger(BudgetAlertController.class);

    private final BudgetAlertService alertService;

    public BudgetAlertController(BudgetAlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Check all active budgets and generate alerts
     * POST /api/budget-alerts/check-all
     */
    @PostMapping("/check-all")
    public ResponseEntity<List<BudgetAlertDTO>> checkAllBudgets() {
        logger.info("Received request to check all budgets");
        
        try {
            List<BudgetAlertDTO> alerts = alertService.checkAllBudgets();
            
            logger.info("Budget check completed. Generated {} alerts", alerts.size());
            return ResponseEntity.ok(alerts);
            
        } catch (Exception e) {
            logger.error("Error checking budgets: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Check a specific budget
     * POST /api/budget-alerts/check/{budgetId}
     */
    @PostMapping("/check/{budgetId}")
    public ResponseEntity<BudgetAlertDTO> checkBudget(@PathVariable Long budgetId) {
        logger.info("Received request to check budget: {}", budgetId);
        
        try {
            Optional<BudgetAlertDTO> alertOpt = alertService.checkBudget(budgetId);
            
            if (alertOpt.isPresent()) {
                logger.info("Alert generated for budget: {}", budgetId);
                return ResponseEntity.ok(alertOpt.get());
            } else {
                // Check if budget exists - if not, return 404
                if (!alertService.budgetExists(budgetId)) {
                    logger.warn("Budget not found: {}", budgetId);
                    return ResponseEntity.notFound().build();
                }
                logger.info("No alert needed for budget: {}", budgetId);
                return ResponseEntity.noContent().build();
            }
            
        } catch (Exception e) {
            logger.error("Error checking budget {}: {}", budgetId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all unread alerts
     * GET /api/budget-alerts/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<List<BudgetAlertDTO>> getUnreadAlerts() {
        logger.info("Received request to get unread alerts");
        
        try {
            List<BudgetAlertDTO> alerts = alertService.getUnreadAlerts();
            logger.info("Retrieved {} unread alerts", alerts.size());
            return ResponseEntity.ok(alerts);
            
        } catch (Exception e) {
            logger.error("Error retrieving unread alerts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get critical unread alerts
     * GET /api/budget-alerts/critical
     */
    @GetMapping("/critical")
    public ResponseEntity<List<BudgetAlertDTO>> getCriticalAlerts() {
        logger.info("Received request to get critical alerts");
        
        try {
            List<BudgetAlertDTO> alerts = alertService.getCriticalAlerts();
            logger.info("Retrieved {} critical alerts", alerts.size());
            return ResponseEntity.ok(alerts);
            
        } catch (Exception e) {
            logger.error("Error retrieving critical alerts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all alerts for a specific budget
     * GET /api/budget-alerts/budget/{budgetId}
     */
    @GetMapping("/budget/{budgetId}")
    public ResponseEntity<List<BudgetAlertDTO>> getAlertsByBudgetId(@PathVariable Long budgetId) {
        logger.info("Received request to get alerts for budget: {}", budgetId);
        
        try {
            List<BudgetAlertDTO> alerts = alertService.getAlertsByBudgetId(budgetId);
            logger.info("Retrieved {} alerts for budget {}", alerts.size(), budgetId);
            return ResponseEntity.ok(alerts);
            
        } catch (Exception e) {
            logger.error("Error retrieving alerts for budget {}: {}", budgetId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all alerts by alert level
     * GET /api/budget-alerts/level/{level}
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<List<BudgetAlertDTO>> getAlertsByLevel(@PathVariable String level) {
        logger.info("Received request to get alerts for level: {}", level);
        
        try {
            List<BudgetAlertDTO> alerts = alertService.getAlertsByLevel(level);
            logger.info("Retrieved {} alerts for level {}", alerts.size(), level);
            return ResponseEntity.ok(alerts);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid alert level: {}", level);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error retrieving alerts for level {}: {}", level, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all alerts
     * GET /api/budget-alerts
     */
    @GetMapping
    public ResponseEntity<List<BudgetAlertDTO>> getAllAlerts() {
        logger.info("Received request to get all alerts");
        
        try {
            List<BudgetAlertDTO> alerts = alertService.getAllAlerts();
            logger.info("Retrieved {} total alerts", alerts.size());
            return ResponseEntity.ok(alerts);
            
        } catch (Exception e) {
            logger.error("Error retrieving all alerts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark an alert as read
     * PUT /api/budget-alerts/{alertId}/read
     */
    @PutMapping("/{alertId}/read")
    public ResponseEntity<BudgetAlertDTO> markAlertAsRead(@PathVariable Long alertId) {
        logger.info("Received request to mark alert as read: {}", alertId);
        
        try {
            Optional<BudgetAlertDTO> alertOpt = alertService.markAlertAsRead(alertId);
            
            if (alertOpt.isPresent()) {
                logger.info("Alert {} marked as read", alertId);
                return ResponseEntity.ok(alertOpt.get());
            } else {
                logger.warn("Alert not found: {}", alertId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error marking alert {} as read: {}", alertId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete an alert
     * DELETE /api/budget-alerts/{alertId}
     */
    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long alertId) {
        logger.info("Received request to delete alert: {}", alertId);
        
        try {
            boolean deleted = alertService.deleteAlert(alertId);
            
            if (deleted) {
                logger.info("Alert {} deleted successfully", alertId);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("Alert not found: {}", alertId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error deleting alert {}: {}", alertId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark all alerts as read
     * PATCH /api/budget-alerts/read-all
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAlertsAsRead() {
        logger.info("Received request to mark all alerts as read");
        
        try {
            int count = alertService.markAllAlertsAsRead();
            
            Map<String, Object> response = new HashMap<>();
            response.put("markedAsRead", count);
            response.put("message", count + " alerts marked as read");
            
            logger.info("{} alerts marked as read", count);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error marking all alerts as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread alert count
     * GET /api/budget-alerts/count/unread
     */
    @GetMapping("/count/unread")
    public ResponseEntity<Map<String, Long>> getUnreadAlertCount() {
        logger.debug("Received request to get unread alert count");
        
        try {
            Long count = alertService.getUnreadAlertCount();
            
            Map<String, Long> response = new HashMap<>();
            response.put("unreadCount", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting unread alert count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
