package com.yourapp.expensetracker.expense_api.dto;

import com.yourapp.expensetracker.expense_api.validation.NoSqlInjection;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login requests
 * @author Eric Gray - Backend Developer
 */
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @NoSqlInjection(message = "Username/email contains invalid characters")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
