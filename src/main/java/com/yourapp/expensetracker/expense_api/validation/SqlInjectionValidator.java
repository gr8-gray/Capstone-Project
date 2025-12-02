package com.yourapp.expensetracker.expense_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Validator to detect and prevent SQL injection patterns in user input
 * @author Eric Gray - Backend Developer
 */
public class SqlInjectionValidator implements ConstraintValidator<NoSqlInjection, String> {

    private static final Logger logger = LoggerFactory.getLogger(SqlInjectionValidator.class);

    // Common SQL injection patterns to detect
    // Note: Patterns are designed to catch injection attempts while allowing normal text
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        // SQL comments (high confidence injection indicator)
        Pattern.compile(".*(-{2}|/\\*|\\*/|#).*"),
        
        // SQL keywords combined with suspicious characters
        Pattern.compile("(?i).*\\b(select|insert|update|delete|drop|create|alter|union)\\b.*(from|into|table|where|;).*"),
        
        // Common SQL injection techniques with operators
        Pattern.compile("(?i).*(\\bor\\b.*=.*|\\band\\b.*=.*|'.*or.*'.*=.*|\".*or.*\".*=.*)"),
        
        // SQL keywords with special characters indicating injection
        Pattern.compile("(?i).*(select|union|insert|update|delete|drop).*(;|--|/\\*|\\*/|'|\"|\\\").*"),
        
        // Hex encoding attempts (likely bypass attempts)
        Pattern.compile(".*0x[0-9a-f]{6,}.*"),
        
        // SQL functions with parentheses (function calls)
        Pattern.compile("(?i).*(concat|char|ascii|substring|database|version|schema|user)\\s*\\(.*\\).*"),
        
        // Semicolon followed by SQL keywords (command chaining)
        Pattern.compile("(?i).*;\\s*(select|insert|update|delete|drop|create|alter|exec).*")
    };

    @Override
    public void initialize(NoSqlInjection constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null or empty strings are valid (use @NotNull/@NotBlank for those checks)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // Check against SQL injection patterns
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(value).matches()) {
                logger.warn("Potential SQL injection detected in input: {}", sanitizeForLog(value));
                return false;
            }
        }

        return true;
    }

    /**
     * Sanitize input for safe logging (truncate and mask)
     */
    private String sanitizeForLog(String input) {
        if (input == null) return "null";
        String sanitized = input.substring(0, Math.min(input.length(), 50));
        return sanitized + (input.length() > 50 ? "... [TRUNCATED]" : "");
    }
}
