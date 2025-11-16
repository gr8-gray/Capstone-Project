package com.yourapp.expensetracker.expense_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service for managing expense categories
 * Provides predefined categories and category validation
 * @author Eric Gray - Backend Developer
 * @author Michael Basye - Database Engineer (Added logging)
 */
@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    // Predefined expense categories
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
        "Food & Dining",
        "Transportation",
        "Shopping",
        "Entertainment",
        "Bills & Utilities", 
        "Healthcare",
        "Education",
        "Travel",
        "Home & Garden",
        "Personal Care",
        "Insurance",
        "Investments",
        "Gifts & Donations",
        "Business",
        "Taxes",
        "Other"
    );

    /**
     * Get all predefined categories
     */
    public List<String> getDefaultCategories() {
        logger.debug("Fetching default categories list");
        return DEFAULT_CATEGORIES;
    }

    /**
     * Validate if a category is valid
     */
    public boolean isValidCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }
        return DEFAULT_CATEGORIES.contains(category) || category.equals("Other");
    }

    /**
     * Get category suggestions based on description
     */
    public String suggestCategory(String description) {
        logger.debug("Suggesting category for description: {}", 
                    description != null ? description.substring(0, Math.min(description.length(), 50)) : "null");
        if (description == null || description.trim().isEmpty()) {
            logger.debug("Empty description, returning 'Other' category");
            return "Other";
        }
        
        String lowerDesc = description.toLowerCase();
        
        // Food & Dining keywords
        if (containsAny(lowerDesc, Arrays.asList("restaurant", "food", "grocery", "dinner", "lunch", "breakfast", 
                "coffee", "pizza", "burger", "meal", "cafe", "bar", "drink"))) {
            return "Food & Dining";
        }
        
        // Transportation keywords
        if (containsAny(lowerDesc, Arrays.asList("gas", "fuel", "uber", "taxi", "bus", "train", "parking", 
                "car", "maintenance", "repair", "oil change", "metro"))) {
            return "Transportation";
        }
        
        // Shopping keywords
        if (containsAny(lowerDesc, Arrays.asList("amazon", "store", "shopping", "clothes", "clothing", 
                "shoes", "electronics", "purchase", "buy", "mall"))) {
            return "Shopping";
        }
        
        // Entertainment keywords
        if (containsAny(lowerDesc, Arrays.asList("movie", "cinema", "game", "concert", "show", "theater", 
                "streaming", "netflix", "spotify", "entertainment"))) {
            return "Entertainment";
        }
        
        // Bills & Utilities keywords
        if (containsAny(lowerDesc, Arrays.asList("electricity", "water", "internet", "phone", "cable", 
                "utility", "bill", "rent", "mortgage"))) {
            return "Bills & Utilities";
        }
        
        // Healthcare keywords
        if (containsAny(lowerDesc, Arrays.asList("doctor", "hospital", "medicine", "pharmacy", "health", 
                "medical", "dentist", "checkup", "prescription"))) {
            return "Healthcare";
        }
        
        return "Other";
    }

    /**
     * Helper method to check if string contains any of the keywords
     */
    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
}