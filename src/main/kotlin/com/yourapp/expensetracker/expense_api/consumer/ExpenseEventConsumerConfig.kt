package com.yourapp.expensetracker.expense_api.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.kafka.dsl.Kafka
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

/**
 * Spring Integration INBOUND wiring for the budget-alert consumer (P3).
 *
 * <p>Flow: Kafka `expenses` topic -> message-driven channel adapter -> transform
 * the raw JSON payload into an [ExpenseEventDto] -> [BudgetEvaluator]. This is the
 * mirror image of the producer's outbound channel adapter from P2; same broker
 * (Kafka), same integration framework (Spring Integration), no Camel/RabbitMQ.</p>
 *
 * <p>Like the producer config it is gated on {@code app.events.enabled=true}
 * (default). In the test profile that flag is false, so this whole configuration
 * — and therefore any live broker connection — is never created, keeping the
 * existing unit tests offline and green.</p>
 *
 * @author Eric Gray - Backend Developer
 */
@Configuration
@ConditionalOnProperty(name = ["app.events.enabled"], havingValue = "true", matchIfMissing = true)
class ExpenseEventConsumerConfig(
    private val objectMapper: ObjectMapper,
) {

    private val log = LoggerFactory.getLogger(ExpenseEventConsumerConfig::class.java)

    /**
     * Plain String consumer factory. The producer publishes type-header-free JSON
     * (`JsonSerializer.noTypeInfo()`), so we read the value as a String and bind it
     * with the shared application [ObjectMapper] (JavaTimeModule + kotlin module),
     * keeping deserialization fully under our control and portable.
     */
    @Bean
    fun expenseEventConsumerFactory(
        @Value("\${spring.kafka.bootstrap-servers:localhost:9092}") bootstrapServers: String,
        @Value("\${app.kafka.consumer.group-id:budget-alert-consumer}") groupId: String,
    ): ConsumerFactory<String, String> {
        val props = mapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        )
        return DefaultKafkaConsumerFactory(props)
    }

    /**
     * Inbound flow: consume the topic, parse the JSON to [ExpenseEventDto], and
     * dispatch to the [BudgetEvaluator]. A malformed payload is logged and dropped
     * rather than poisoning the consumer.
     */
    @Bean
    fun expenseEventInboundFlow(
        consumerFactory: ConsumerFactory<String, String>,
        budgetEvaluator: BudgetEvaluator,
        @Value("\${app.kafka.topic.expenses:expenses}") topic: String,
    ): IntegrationFlow {
        val adapter = Kafka.messageDrivenChannelAdapter(consumerFactory, topic)
        return integrationFlow(adapter) {
            handle { message ->
                val payload = message.payload as String
                runCatching { objectMapper.readValue(payload, ExpenseEventDto::class.java) }
                    .onSuccess { event ->
                        log.info(
                            "Consumed {} event {} (expense {}, user {}, category '{}', amount {})",
                            event.eventType, event.eventId, event.expenseId,
                            event.userId, event.category, event.amount,
                        )
                        budgetEvaluator.evaluate(event)
                    }
                    .onFailure { ex ->
                        log.error("Failed to parse expense event payload, dropping: {}", payload, ex)
                    }
            }
        }
    }
}
