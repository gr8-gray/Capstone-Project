package com.yourapp.expensetracker.expense_api.consumer

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * Kotlin view of the wire contract published by the Java producer
 * (`com.yourapp.expensetracker.expense_api.event.ExpenseEvent`).
 *
 * The producer serializes that record with the application ObjectMapper using
 * `JsonSerializer(...).noTypeInfo()`, so the message on the `expenses` topic is
 * plain, portable JSON with ISO-8601 dates and no Kafka type headers. We mirror
 * the exact field shape here and let `jackson-module-kotlin` bind it. Field names
 * line up 1:1 with the producer record components:
 *
 *   eventId, eventType, expenseId, userId, description, amount, category, date, occurredAt
 *
 * @author Eric Gray - Backend Developer
 */
data class ExpenseEventDto(
    val eventId: String,
    val eventType: EventType,
    val expenseId: Long,
    val userId: Long?,
    val description: String?,
    val amount: BigDecimal,
    val category: String,
    val date: LocalDate,
    val occurredAt: Instant,
) {
    enum class EventType {
        EXPENSE_CREATED,
        EXPENSE_UPDATED,
    }
}
