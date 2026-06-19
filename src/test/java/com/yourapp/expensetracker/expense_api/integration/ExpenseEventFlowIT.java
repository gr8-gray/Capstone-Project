package com.yourapp.expensetracker.expense_api.integration;

import com.yourapp.expensetracker.expense_api.model.Budget;
import com.yourapp.expensetracker.expense_api.model.BudgetAlert;
import com.yourapp.expensetracker.expense_api.model.Expense;
import com.yourapp.expensetracker.expense_api.model.User;
import com.yourapp.expensetracker.expense_api.repository.BudgetAlertRepository;
import com.yourapp.expensetracker.expense_api.repository.BudgetRepository;
import com.yourapp.expensetracker.expense_api.repository.UserRepository;
import com.yourapp.expensetracker.expense_api.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Phase 4 — full event-driven integration test.
 *
 * <p>Proves the real publish -> consume -> alert loop end to end against ephemeral
 * containers (no mocks, no hand-calling the consumer):</p>
 * <ol>
 *   <li>A {@link KafkaContainer} and {@link MySQLContainer} are started by Testcontainers
 *       (random host ports, so no clash with the dev stack on 3306/9092).</li>
 *   <li>{@link DynamicPropertySource} points the app at those containers and flips
 *       {@code app.events.enabled=true}, so the P2 producer flow AND the P3 Kotlin
 *       consumer flow both come up — unlike the unit-test profile which keeps them off.</li>
 *   <li>The test seeds a user + a budget, then saves an over-budget expense through
 *       {@link ExpenseService#createExpense} (the same service path the REST layer uses,
 *       which publishes the {@code ExpenseEvent} to Kafka).</li>
 *   <li>It awaits (up to 30s) until the Kotlin {@code BudgetEvaluator} has consumed the
 *       event and persisted a {@link BudgetAlert}, then asserts the breach is CRITICAL,
 *       over 100%, with the correct spent/limit amounts.</li>
 * </ol>
 *
 * <p>This is the ONLY test that brings up containers and enables eventing. The existing
 * 155 tests stay offline/green under the {@code test} profile ({@code app.events.enabled=false}).
 * Named {@code *IT} so it runs under failsafe via {@code mvn verify}, not surefire.</p>
 *
 * @author Eric Gray - Backend Developer
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ExpenseEventFlowIT {

    private static final String CATEGORY = "Food";
    private static final BigDecimal BUDGET_LIMIT = new BigDecimal("100.00");
    private static final BigDecimal OVER_BUDGET_AMOUNT = new BigDecimal("150.00");

    @Container
    static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName("expense_db")
                    .withUsername("expense_user")
                    .withPassword("expense_password");

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.9.1"));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // --- MySQL (random host port mapped by Testcontainers) ---
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // Hibernate auto-creates the schema in the fresh container.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");

        // --- Kafka bootstrap-servers the app actually reads ---
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);

        // --- Turn the whole event-driven path ON for this test only ---
        registry.add("app.events.enabled", () -> "true");
    }

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetAlertRepository alertRepository;

    @Test
    void overBudgetExpense_publishesEvent_consumerPersistsCriticalAlert() {
        // --- Seed: a user owning a budget for the category under test ---
        User user = new User();
        user.setUsername("it-user");
        user.setEmail("it-user@example.com");
        user.setPasswordHash("not-a-real-hash");
        user.setFirstName("IT");
        user.setLastName("User");
        user.setRole("USER");
        user.setActive(true);
        user = userRepository.save(user);

        LocalDate today = LocalDate.now();
        Budget budget = new Budget(CATEGORY, BUDGET_LIMIT, today.minusDays(15), today.plusDays(15));
        budget.setUser(user);
        budgetRepository.save(budget);

        // --- Drive the REAL flow: create an over-budget expense through the service
        //     layer, which publishes the ExpenseEvent to Kafka (P2 producer). ---
        Expense expense = new Expense("Big grocery run", OVER_BUDGET_AMOUNT, CATEGORY, today);
        expense.setUser(user);
        expenseService.createExpense(expense);

        // --- Await the P3 Kotlin consumer persisting a BudgetAlert for this budget. ---
        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<BudgetAlert> alerts = alertRepository.findAll();
                    assertThat(alerts)
                            .as("BudgetAlert persisted by the Kotlin consumer after consuming the expense event")
                            .anySatisfy(alert -> {
                                // Assert on BudgetAlert's own columns + message, NOT the LAZY
                                // budget relation (which can't initialize on a detached entity
                                // outside a Hibernate session — the cause of the first run's
                                // LazyInitializationException). The fresh container holds only
                                // this run's data and the consumer embeds the category in the
                                // message, so these uniquely identify the breach.
                                assertThat(alert.getMessage()).contains(CATEGORY);
                                assertThat(alert.getAlertLevel()).isEqualTo(BudgetAlert.AlertLevel.CRITICAL);
                                assertThat(alert.getPercentageUsed())
                                        .isGreaterThan(new BigDecimal("100.00"));
                                assertThat(alert.getSpentAmount()).isEqualByComparingTo(OVER_BUDGET_AMOUNT);
                                assertThat(alert.getBudgetLimit()).isEqualByComparingTo(BUDGET_LIMIT);
                            });
                });
    }
}
