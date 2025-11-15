package com.yourapp.expensetracker.expense_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a budget alert notification
 * @author Eric Gray - Backend Developer
 */
@Entity
@Table(name = "budget_alerts")
public class BudgetAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;

    @Column(nullable = false)
    private String message;

    @Column(name = "spent_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal spentAmount;

    @Column(name = "budget_limit", nullable = false, precision = 12, scale = 2)
    private BigDecimal budgetLimit;

    @Column(name = "percentage_used", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentageUsed;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // --- Enums ---

    public enum AlertLevel {
        INFO,      // 50% threshold
        WARNING,   // 75% threshold
        DANGER,    // 90% threshold
        CRITICAL   // 100% or over threshold
    }

    // --- Constructors ---

    public BudgetAlert() {
    }

    public BudgetAlert(Budget budget, AlertLevel alertLevel, String message,
                       BigDecimal spentAmount, BigDecimal budgetLimit, BigDecimal percentageUsed) {
        this.budget = budget;
        this.alertLevel = alertLevel;
        this.message = message;
        this.spentAmount = spentAmount;
        this.budgetLimit = budgetLimit;
        this.percentageUsed = percentageUsed;
        this.isRead = false;
    }

    // --- Lifecycle callbacks ---

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
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

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
        if (isRead && this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
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

    @Override
    public String toString() {
        return "BudgetAlert{" +
                "id=" + id +
                ", alertLevel=" + alertLevel +
                ", message='" + message + '\'' +
                ", spentAmount=" + spentAmount +
                ", budgetLimit=" + budgetLimit +
                ", percentageUsed=" + percentageUsed +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}
