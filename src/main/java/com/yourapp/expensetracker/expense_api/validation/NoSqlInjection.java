package com.yourapp.expensetracker.expense_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to prevent SQL injection patterns
 * @author Eric Gray - Backend Developer
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SqlInjectionValidator.class)
@Documented
public @interface NoSqlInjection {
    
    String message() default "Invalid input: potential SQL injection detected";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
