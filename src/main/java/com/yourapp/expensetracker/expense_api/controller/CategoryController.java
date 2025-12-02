package com.yourapp.expensetracker.expense_api.controller;

import com.yourapp.expensetracker.expense_api.model.Category;
import com.yourapp.expensetracker.expense_api.repository.CategoryRepository;
import com.yourapp.expensetracker.expense_api.service.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Category CRUD operations
 * Handles HTTP requests for category management
 * @author Eric Gray - Backend Developer
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryRepository categoryRepository, CategoryService categoryService) {
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    /**
     * Create a new category
     * POST /api/categories
     */
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody Category category) {
        logger.info("Creating new category: {}", category.getName());
        try {
            // Check if category already exists
            if (categoryRepository.existsByName(category.getName())) {
                logger.warn("Category already exists: {}", category.getName());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Category already exists"));
            }

            Category savedCategory = categoryRepository.save(category);
            logger.info("Category created successfully with ID: {}", savedCategory.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
        } catch (Exception e) {
            logger.error("Failed to create category: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create category"));
        }
    }

    /**
     * Get all categories
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        logger.debug("Fetching all categories");
        try {
            List<Category> categories = categoryRepository.findAll();
            logger.info("Retrieved {} categories", categories.size());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Failed to fetch categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch categories"));
        }
    }

    /**
     * Get category by ID
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        logger.debug("Fetching category with ID: {}", id);
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isPresent()) {
                Category category = categoryOpt.get();
                logger.debug("Category found: {}", category.getName());
                return ResponseEntity.ok(category);
            } else {
                logger.warn("Category not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Category not found"));
            }
        } catch (Exception e) {
            logger.error("Failed to fetch category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch category"));
        }
    }

    /**
     * Update category
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody Category categoryDetails) {
        logger.info("Updating category with ID: {}", id);
        try {
            return categoryRepository.findById(id)
                    .map(existingCategory -> {
                        // Check if new name conflicts with another category
                        if (!existingCategory.getName().equals(categoryDetails.getName()) &&
                            categoryRepository.existsByName(categoryDetails.getName())) {
                            logger.warn("Category name already exists: {}", categoryDetails.getName());
                            return ResponseEntity.status(HttpStatus.CONFLICT)
                                    .body(Map.of("error", "Category name already exists"));
                        }

                        existingCategory.setName(categoryDetails.getName());
                        if (categoryDetails.getDescription() != null) {
                            existingCategory.setDescription(categoryDetails.getDescription());
                        }
                        Category updatedCategory = categoryRepository.save(existingCategory);
                        logger.info("Category updated successfully: {}", updatedCategory.getName());
                        return ResponseEntity.ok(updatedCategory);
                    })
                    .orElseGet(() -> {
                        logger.warn("Category not found with ID: {}", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", "Category not found"));
                    });
        } catch (Exception e) {
            logger.error("Failed to update category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update category"));
        }
    }

    /**
     * Delete category
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        logger.info("Deleting category with ID: {}", id);
        try {
            if (!categoryRepository.existsById(id)) {
                logger.warn("Category not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Category not found"));
            }

            categoryRepository.deleteById(id);
            logger.info("Category deleted successfully with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete category"));
        }
    }

    /**
     * Get predefined/default categories
     * GET /api/categories/defaults
     */
    @GetMapping("/defaults")
    public ResponseEntity<?> getDefaultCategories() {
        logger.debug("Fetching default categories");
        try {
            List<String> defaults = categoryService.getDefaultCategories();
            logger.info("Retrieved {} default categories", defaults.size());
            return ResponseEntity.ok(Map.of("categories", defaults));
        } catch (Exception e) {
            logger.error("Failed to fetch default categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch default categories"));
        }
    }

    /**
     * Search categories by name
     * GET /api/categories/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchCategories(@RequestParam String name) {
        logger.debug("Searching categories with name containing: {}", name);
        try {
            List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
            logger.info("Found {} categories matching '{}'", categories.size(), name);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Failed to search categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search categories"));
        }
    }

    /**
     * Get category count
     * GET /api/categories/count
     */
    @GetMapping("/count")
    public ResponseEntity<?> getCategoryCount() {
        logger.debug("Getting category count");
        try {
            long count = categoryRepository.count();
            logger.info("Total categories: {}", count);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            logger.error("Failed to get category count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get category count"));
        }
    }

    /**
     * Check if category name exists
     * GET /api/categories/exists?name={name}
     */
    @GetMapping("/exists")
    public ResponseEntity<?> checkCategoryExists(@RequestParam String name) {
        logger.debug("Checking if category exists: {}", name);
        try {
            boolean exists = categoryRepository.existsByName(name);
            logger.debug("Category '{}' exists: {}", name, exists);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            logger.error("Failed to check category existence: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check category"));
        }
    }
}
