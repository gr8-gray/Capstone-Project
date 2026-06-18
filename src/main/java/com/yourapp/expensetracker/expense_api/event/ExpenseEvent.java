package com.yourapp.expensetracker.expense_api.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Domain event published to Kafka when an expense is created or updated.
 *
 * <p>This is the wire contract consumed by the (Kotlin) budget-alert consumer
 * service. It is intentionally a flat, self-contained record so the consumer
 * can evaluate budget thresholds without calling back into this service.</p>
 *
 * @author Eric Gray - Backend Developer
 */
public record ExpenseEvent(
        String eventId,
        EventType eventType,
        Long expenseId,
        Long userId,
        String description,
        BigDecimal amount,
        String category,
        LocalDate date,
        Instant occurredAt
) {
    public enum EventType {
        EXPENSE_CREATED,
        EXPENSE_UPDATED
    }
}
