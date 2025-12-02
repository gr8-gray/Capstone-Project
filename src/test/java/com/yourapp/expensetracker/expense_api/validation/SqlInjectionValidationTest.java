package com.yourapp.expensetracker.expense_api.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import com.yourapp.expensetracker.expense_api.model.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SQL injection prevention validation
 * @author Eric Gray - Backend Developer
 */
@DisplayName("SQL Injection Prevention Tests")
public class SqlInjectionValidationTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    @DisplayName("Should allow normal expense descriptions")
    public void shouldAllowNormalDescriptions() {
        Expense expense = new Expense(
            "Grocery shopping at Walmart",
            new BigDecimal("50.00"),
            "Food",
            LocalDate.now()
        );

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertTrue(violations.isEmpty(), "Normal description should be valid");
    }

    @Test
    @DisplayName("Should allow description with word 'delete'")
    public void shouldAllowDescriptionWithDeleteWord() {
        Expense expense = new Expense(
            "To delete later",
            new BigDecimal("50.00"),
            "Food",
            LocalDate.now()
        );

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertTrue(violations.isEmpty(), "Description with 'delete' word should be valid");
    }

    @Test
    @DisplayName("Should reject SQL comment injection")
    public void shouldRejectSqlCommentInjection() {
        Expense expense = new Expense(
            "Test expense-- DROP TABLE expenses",
            new BigDecimal("50.00"),
            "Food",
            LocalDate.now()
        );

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty(), "SQL comment should be rejected");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("invalid characters")));
    }

    @Test
    @DisplayName("Should reject UNION SELECT injection")
    public void shouldRejectUnionSelectInjection() {
        Expense expense = new Expense(
            "Test",
            new BigDecimal("50.00"),
            "Food' UNION SELECT * FROM users--",
            LocalDate.now()
        );

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty(), "UNION SELECT should be rejected");
    }

    @Test
    @DisplayName("Should reject OR equals injection")
    public void shouldRejectOrEqualsInjection() {
        Expense expense = new Expense(
            "Test' OR '1'='1",
            new BigDecimal("50.00"),
            "Food",
            LocalDate.now()
        );

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty(), "OR equals injection should be rejected");
    }

    @Test
    @DisplayName("Should reject semicolon command chaining")
    public void shouldRejectSemicolonCommandChaining() {
        Expense expense = new Expense(
            "Test; DROP TABLE expenses",
            new BigDecimal("50.00"),
            "Food",
            LocalDate.now()
        );

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty(), "Command chaining should be rejected");
    }

    @Test
    @DisplayName("Should reject hex encoding bypass")
    public void shouldRejectHexEncodingBypass() {
        Expense expense = new Expense(
            "Test 0x53454c454354",
            new BigDecimal("50.00"),
            "Food",
            LocalDate.now()
        );

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty(), "Hex encoding should be rejected");
    }

    @Test
    @DisplayName("Should reject SQL function injection")
    public void shouldRejectSqlFunctionInjection() {
        Expense expense = new Expense(
            "Test database() function",
            new BigDecimal("50.00"),
            "Food",
            LocalDate.now()
        );

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty(), "SQL function call should be rejected");
    }

    @Test
    @DisplayName("Should reject multiline comment injection")
    public void shouldRejectMultilineCommentInjection() {
        Expense expense = new Expense(
            "Test /* comment */ expense",
            new BigDecimal("50.00"),
            "Food",
            LocalDate.now()
        );

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty(), "Multiline comment should be rejected");
    }

    @Test
    @DisplayName("Should allow normal category names")
    public void shouldAllowNormalCategories() {
        Expense expense = new Expense(
            "Test expense",
            new BigDecimal("50.00"),
            "Food & Dining",
            LocalDate.now()
        );

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertTrue(violations.isEmpty(), "Normal category should be valid");
    }

    @Test
    @DisplayName("Should reject DROP TABLE injection")
    public void shouldRejectDropTableInjection() {
        Expense expense = new Expense(
            "Test",
            new BigDecimal("50.00"),
            "'; DROP TABLE expenses;--",
            LocalDate.now()
        );

        Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
        assertFalse(violations.isEmpty(), "DROP TABLE should be rejected");
    }
}
