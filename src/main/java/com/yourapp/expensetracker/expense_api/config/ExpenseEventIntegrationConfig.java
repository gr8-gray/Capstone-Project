package com.yourapp.expensetracker.expense_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.MessageChannel;

/**
 * Spring Integration wiring for the expense event producer.
 *
 * <p>Flow: service layer -> {@code ExpenseEventPublisher} ->
 * {@code expenseEventsOut} channel -> Kafka outbound channel adapter ->
 * {@code expenses} topic. The publisher and flow are only created when
 * {@code app.events.enabled=true} (default), keeping broker-less unit tests
 * fast and offline.</p>
 *
 * @author Eric Gray - Backend Developer
 */
@Configuration
@ConditionalOnProperty(name = "app.events.enabled", havingValue = "true", matchIfMissing = true)
public class ExpenseEventIntegrationConfig {

    /**
     * Serialize event values with the application's Jackson mapper (JavaTimeModule,
     * ISO-8601 dates) instead of the Kafka serializer's bare default mapper, so the
     * consumer receives {@code "2026-06-18"} rather than {@code [2026,6,18]}.
     */
    @Bean
    public DefaultKafkaProducerFactoryCustomizer eventProducerFactoryCustomizer(ObjectMapper objectMapper) {
        return producerFactory -> {
            @SuppressWarnings("unchecked")
            DefaultKafkaProducerFactory<String, Object> pf =
                    (DefaultKafkaProducerFactory<String, Object>) producerFactory;
            pf.setValueSerializer(new JsonSerializer<>(objectMapper).noTypeInfo());
        };
    }

    /** Application channel the gateway sends events to. */
    @Bean
    public MessageChannel expenseEventsOut() {
        return new DirectChannel();
    }

    /** Auto-created on startup by the Kafka admin so the topic exists for demos. */
    @Bean
    public NewTopic expensesTopic(@Value("${app.kafka.topic.expenses:expenses}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }

    /** Bridges the application channel to the Kafka topic. */
    @Bean
    public IntegrationFlow expenseEventOutboundFlow(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topic.expenses:expenses}") String topic) {
        return IntegrationFlow.from("expenseEventsOut")
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate).topic(topic))
                .get();
    }
}
