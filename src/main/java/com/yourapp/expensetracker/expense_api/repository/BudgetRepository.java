package com.yourapp.expensetracker.expense_api.repository;

import com.yourapp.expensetracker.expense_api.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByCategory(String category);

    List<Budget> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate startDate, LocalDate endDate);
}