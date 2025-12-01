package com.yourapp.expensetracker.expense_api.dto;

public class ResetPasswordRequest {

    private String email;
    private String answer;
    private String newPassword;

    public String getEmail() {
        return email;
    }

    public String getAnswer() {
        return answer;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
