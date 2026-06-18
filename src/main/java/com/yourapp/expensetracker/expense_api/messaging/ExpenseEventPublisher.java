package com.yourapp.expensetracker.expense_api.messaging;

import com.yourapp.expensetracker.expense_api.event.ExpenseEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Publishes expense domain events onto the Spring Integration
 * {@code expenseEventsOut} channel, which an
 * {@link org.springframework.integration.dsl.IntegrationFlow} bridges to the
 * Kafka {@code expenses} topic via an outbound channel adapter.
 *
 * <p>Only registered when {@code app.events.enabled=true} (default). In the
 * test profile it is absent, so the service layer (which depends on it via
 * {@code ObjectProvider}) simply skips publishing — no broker required.</p>
 *
 * @author Eric Gray - Backend Developer
 */
@Component
@ConditionalOnProperty(name = "app.events.enabled", havingValue = "true", matchIfMissing = true)
public class ExpenseEventPublisher {

    private final MessageChannel expenseEventsOut;

    public ExpenseEventPublisher(@Qualifier("expenseEventsOut") MessageChannel expenseEventsOut) {
        this.expenseEventsOut = expenseEventsOut;
    }

    public void publish(ExpenseEvent event) {
        expenseEventsOut.send(MessageBuilder.withPayload(event).build());
    }
}
