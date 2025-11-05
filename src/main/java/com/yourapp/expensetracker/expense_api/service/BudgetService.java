package com.yourapp.expensetracker.expense_api.service;

import com.yourapp.expensetracker.expense_api.model.Budget;
import com.yourapp.expensetracker.expense_api.repository.BudgetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    // CREATE
    public Budget createBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    // READ - all
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    // READ - by id
    public Optional<Budget> getBudgetById(Long id) {
        return budgetRepository.findById(id);
    }

    // READ - active budgets for a period
    public List<Budget> getActiveBudgetsForPeriod(LocalDate start, LocalDate end) {
        return budgetRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(start, end);
    }

    // UPDATE
    public Budget updateBudget(Long id, Budget updated) {
        return budgetRepository.findById(id)
                .map(existing -> {
                    existing.setCategory(updated.getCategory());
                    existing.setLimitAmount(updated.getLimitAmount());
                    existing.setStartDate(updated.getStartDate());
                    existing.setEndDate(updated.getEndDate());
                    return budgetRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Budget with id " + id + " not found"));
    }

    // DELETE
    public void deleteBudget(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new RuntimeException("Budget with id " + id + " not found");
        }
        budgetRepository.deleteById(id);
    }
}