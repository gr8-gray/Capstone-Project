package com.yourapp.expensetracker.expense_api.repository;

import com.yourapp.expensetracker.expense_api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity operations
 * @author Eric Gray - Backend Developer
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);

    /**
     * Check if category exists by name
     */
    boolean existsByName(String name);

    /**
     * Find categories by name containing keyword (case-insensitive)
     */
    List<Category> findByNameContainingIgnoreCase(String keyword);
}