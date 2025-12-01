package com.yourapp.expensetracker.expense_api.dto;

public class SecretQuestionResponse {
    private String question;

    public SecretQuestionResponse(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
