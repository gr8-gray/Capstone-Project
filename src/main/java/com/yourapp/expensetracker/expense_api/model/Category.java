package com.yourapp.expensetracker.expense_api.model;

import com.yourapp.expensetracker.expense_api.validation.NoSqlInjection;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Category entity representing expense categories
 * @author Eric Gray - Backend Developer
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 80, message = "Category name must not exceed 80 characters")
    @NoSqlInjection(message = "Category name contains invalid characters")
    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}