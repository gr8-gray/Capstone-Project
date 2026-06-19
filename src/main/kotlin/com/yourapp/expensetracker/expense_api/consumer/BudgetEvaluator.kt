package com.yourapp.expensetracker.expense_api.consumer

import com.yourapp.expensetracker.expense_api.model.Budget
import com.yourapp.expensetracker.expense_api.model.BudgetAlert
import com.yourapp.expensetracker.expense_api.model.BudgetAlert.AlertLevel
import com.yourapp.expensetracker.expense_api.repository.BudgetAlertRepository
import com.yourapp.expensetracker.expense_api.repository.BudgetRepository
import com.yourapp.expensetracker.expense_api.repository.ExpenseRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Evaluates a consumed [ExpenseEventDto] against the user's budgets and raises a
 * budget-breach alert when spend crosses the threshold.
 *
 * <p>This closes the event-driven loop started by the Java producer (P2): an
 * expense create/update is published to the `expenses` Kafka topic, the Spring
 * Integration inbound adapter (see [ExpenseEventConsumerConfig]) hands the JSON
 * to this evaluator, and on a breach we persist a [BudgetAlert] (the same entity
 * the REST side uses) and log a structured alert.</p>
 *
 * <p>The consumer is a separate concern from the producer-side
 * `BudgetAlertService`: it runs off the event stream with no security context
 * (the user id arrives on the event), so it queries budgets/expenses directly by
 * the event's `userId` rather than the authenticated principal.</p>
 *
 * @author Eric Gray - Backend Developer
 */
@Service
class BudgetEvaluator(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val alertRepository: BudgetAlertRepository,
) {

    private val log = LoggerFactory.getLogger(BudgetEvaluator::class.java)

    private companion object {
        val HUNDRED: BigDecimal = BigDecimal("100")
        val THRESHOLD_INFO = BigDecimal("50")
        val THRESHOLD_WARNING = BigDecimal("75")
        val THRESHOLD_DANGER = BigDecimal("90")
        val THRESHOLD_BREACH = BigDecimal("100")
    }

    /**
     * Evaluate every active budget matching the event's user + category. An alert
     * row is persisted whenever a threshold (>= 50%) is reached; a breach
     * (>= 100%, [AlertLevel.CRITICAL]) is additionally surfaced as a WARN-level
     * structured alert log line.
     */
    @Transactional
    fun evaluate(event: ExpenseEventDto) {
        val userId = event.userId
        if (userId == null) {
            log.warn("Expense event {} has no userId; cannot evaluate budgets", event.eventId)
            return
        }

        val budgets: List<Budget> = budgetRepository
            .findByUserIdAndCategory(userId, event.category)
            .filter { event.date >= it.startDate && event.date <= it.endDate }

        if (budgets.isEmpty()) {
            log.debug(
                "No active budget for user {} category '{}' on {}; nothing to evaluate (event {})",
                userId, event.category, event.date, event.eventId,
            )
            return
        }

        budgets.forEach { budget -> evaluateBudget(budget, userId) }
    }

    private fun evaluateBudget(budget: Budget, userId: Long) {
        val spent: BigDecimal = expenseRepository
            .sumAmountByUserIdAndCategoryAndDateRange(
                userId, budget.category, budget.startDate, budget.endDate,
            ) ?: BigDecimal.ZERO

        val limit = budget.limitAmount
        val percentUsed = spent
            .divide(limit, 4, RoundingMode.HALF_UP)
            .multiply(HUNDRED)
            .setScale(2, RoundingMode.HALF_UP)

        val level = determineLevel(percentUsed)
        if (level == null) {
            log.debug(
                "Budget '{}' (user {}) within limit: spent {} of {} ({}%)",
                budget.category, userId, spent, limit, percentUsed,
            )
            return
        }

        val message = buildMessage(budget.category, spent, limit, percentUsed, level)
        alertRepository.save(BudgetAlert(budget, level, message, spent, limit, percentUsed))

        if (level == AlertLevel.CRITICAL) {
            // Budget breach: the headline alert this consumer exists to produce.
            log.warn(
                "BUDGET BREACH [user={}] category='{}' spent={} limit={} ({}%) - {}",
                userId, budget.category, spent, limit, percentUsed, message,
            )
        } else {
            log.info(
                "Budget threshold {} [user={}] category='{}' ({}%) - {}",
                level, userId, budget.category, percentUsed, message,
            )
        }
    }

    private fun determineLevel(percentUsed: BigDecimal): AlertLevel? = when {
        percentUsed >= THRESHOLD_BREACH -> AlertLevel.CRITICAL
        percentUsed >= THRESHOLD_DANGER -> AlertLevel.DANGER
        percentUsed >= THRESHOLD_WARNING -> AlertLevel.WARNING
        percentUsed >= THRESHOLD_INFO -> AlertLevel.INFO
        else -> null
    }

    private fun buildMessage(
        category: String,
        spent: BigDecimal,
        limit: BigDecimal,
        percentUsed: BigDecimal,
        level: AlertLevel,
    ): String {
        val remaining = limit.subtract(spent)
        return when (level) {
            AlertLevel.CRITICAL ->
                if (remaining < BigDecimal.ZERO) {
                    "CRITICAL: '$category' budget exceeded by \$${remaining.abs()}! " +
                        "Spent: \$$spent of \$$limit ($percentUsed%)"
                } else {
                    "CRITICAL: '$category' budget limit reached! " +
                        "Spent: \$$spent of \$$limit ($percentUsed%)"
                }
            AlertLevel.DANGER ->
                "DANGER: '$category' budget at $percentUsed%! Only \$$remaining remaining of \$$limit budget"
            AlertLevel.WARNING ->
                "WARNING: '$category' budget at $percentUsed%. Spent: \$$spent of \$$limit (\$$remaining remaining)"
            AlertLevel.INFO ->
                "INFO: '$category' budget halfway mark reached. Spent: \$$spent of \$$limit ($percentUsed%)"
        }
    }
}
