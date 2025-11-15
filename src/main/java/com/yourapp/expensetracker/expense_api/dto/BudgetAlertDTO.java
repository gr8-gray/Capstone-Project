package com.yourapp.expensetracker.expense_api.dto;

import com.yourapp.expensetracker.expense_api.model.BudgetAlert;
import com.yourapp.expensetracker.expense_api.model.BudgetAlert.AlertLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for BudgetAlert
 * @author Eric Gray - Backend Developer
 */
public class BudgetAlertDTO {

    private Long id;
    private Long budgetId;
    private String budgetCategory;
    private AlertLevel alertLevel;
    private String message;
    private BigDecimal spentAmount;
    private BigDecimal budgetLimit;
    private BigDecimal percentageUsed;
    private BigDecimal remainingAmount;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // --- Constructors ---

    public BudgetAlertDTO() {
    }

    public BudgetAlertDTO(BudgetAlert alert) {
        this.id = alert.getId();
        this.budgetId = alert.getBudget().getId();
        this.budgetCategory = alert.getBudget().getCategory();
        this.alertLevel = alert.getAlertLevel();
        this.message = alert.getMessage();
        this.spentAmount = alert.getSpentAmount();
        this.budgetLimit = alert.getBudgetLimit();
        this.percentageUsed = alert.getPercentageUsed();
        this.remainingAmount = alert.getBudgetLimit().subtract(alert.getSpentAmount());
        this.isRead = alert.getIsRead();
        this.createdAt = alert.getCreatedAt();
        this.readAt = alert.getReadAt();
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public String getBudgetCategory() {
        return budgetCategory;
    }

    public void setBudgetCategory(String budgetCategory) {
        this.budgetCategory = budgetCategory;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(BigDecimal budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    public BigDecimal getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(BigDecimal percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
