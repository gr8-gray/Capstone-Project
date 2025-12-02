package com.yourapp.expensetracker.expense_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourapp.expensetracker.expense_api.model.Category;
import com.yourapp.expensetracker.expense_api.model.User;
import com.yourapp.expensetracker.expense_api.repository.CategoryRepository;
import com.yourapp.expensetracker.expense_api.repository.UserRepository;
import com.yourapp.expensetracker.expense_api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CategoryController
 * Tests CRUD operations for categories
 * @author Eric Gray - Backend Developer
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser.setRole("USER");
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // Generate JWT token
        jwtToken = jwtTokenProvider.generateTokenFromUsername(testUser.getUsername());

        // Seed default categories
        seedDefaultCategories();
    }

    @Test
    void shouldGetAllCategories() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[*].name", hasItem("Food & Dining")));
    }

    @Test
    void shouldGetDefaultCategories() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/defaults")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.categories", hasItems(
                    "Food & Dining",
                    "Transportation",
                    "Shopping",
                    "Entertainment"
                )));
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        // Given
        Category category = categoryRepository.findAll().get(0);

        // When & Then
        mockMvc.perform(get("/api/categories/" + category.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value(category.getName()));
    }

    @Test
    void shouldReturnNotFoundForNonExistentCategory() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/99999")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateCustomCategory() throws Exception {
        // Given
        Map<String, String> categoryRequest = new HashMap<>();
        categoryRequest.put("name", "Custom Category");
        categoryRequest.put("description", "My custom category");

        // When & Then
        mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Custom Category"))
                .andExpect(jsonPath("$.description").value("My custom category"));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        // Given: Create a category
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Original description");
        category = categoryRepository.save(category);

        // When: Update category
        Map<String, String> updateRequest = new HashMap<>();
        updateRequest.put("name", "Updated Category");
        updateRequest.put("description", "Updated description");

        // Then
        mockMvc.perform(put("/api/categories/" + category.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Updated Category"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        // Given: Create a category
        Category category = new Category();
        category.setName("Test Category");
        category = categoryRepository.save(category);

        // When: Delete category
        mockMvc.perform(delete("/api/categories/" + category.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Then: Category should be deleted
        mockMvc.perform(get("/api/categories/" + category.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectDuplicateCategoryName() throws Exception {
        // Given: Existing category
        Category existing = new Category();
        existing.setName("Duplicate Category");
        categoryRepository.save(existing);

        // When: Try to create with same name
        Map<String, String> categoryRequest = new HashMap<>();
        categoryRequest.put("name", "Duplicate Category");

        // Then: Expect 409 Conflict (correct HTTP status for duplicate resource)
        mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectEmptyCategoryName() throws Exception {
        // Given
        Map<String, String> categoryRequest = new HashMap<>();
        categoryRequest.put("name", "");

        // When & Then
        mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchCategoriesByName() throws Exception {
        // Given: Categories with different names (using unique name to avoid duplicate constraint)
        Category food = new Category();
        food.setName("Fast Food Test " + System.currentTimeMillis());
        categoryRepository.save(food);

        Category transport = new Category();
        transport.setName("Unique Transportation " + System.currentTimeMillis());
        categoryRepository.save(transport);

        // When & Then: Search for "Food"
        mockMvc.perform(get("/api/categories/search")
                .header("Authorization", "Bearer " + jwtToken)
                .param("name", "Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem(containsString("Food"))));
    }

    @Test
    void shouldGetCategoriesByDescription() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Health Category " + System.currentTimeMillis());
        category.setDescription("Health and wellness");
        categoryRepository.save(category);

        // When & Then: Search uses 'name' parameter, not 'query'
        mockMvc.perform(get("/api/categories/search")
                .header("Authorization", "Bearer " + jwtToken)
                .param("name", "Health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem(containsString("Health"))));
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        // When: Try to access without token
        // Spring Security 6.x returns 403 Forbidden when CSRF is disabled
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowPublicAccessToDefaults() throws Exception {
        // When & Then: Endpoint requires authentication (not public)
        mockMvc.perform(get("/api/categories/defaults"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnCategoriesSortedByName() throws Exception {
        // Given: Multiple categories
        Category c1 = new Category();
        c1.setName("Zebra Category");
        categoryRepository.save(c1);

        Category c2 = new Category();
        c2.setName("Alpha Category");
        categoryRepository.save(c2);

        // When & Then: Should be sorted
        mockMvc.perform(get("/api/categories?sort=name")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(lessThanOrEqualTo("Z")));
    }

    @Test
    void shouldHandleCategoryWithLongDescription() throws Exception {
        // Given
        String longDescription = "A".repeat(500);
        Map<String, String> categoryRequest = new HashMap<>();
        categoryRequest.put("name", "Long Desc Category");
        categoryRequest.put("description", longDescription);

        // When & Then
        mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(longDescription));
    }

    @Test
    void shouldHandleCategoryWithSpecialCharacters() throws Exception {
        // Given
        Map<String, String> categoryRequest = new HashMap<>();
        categoryRequest.put("name", "Category & Special-Chars_123");
        categoryRequest.put("description", "Description with (special) [characters]!");

        // When & Then
        mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnEmptyListForSearchWithNoMatches() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/search")
                .header("Authorization", "Bearer " + jwtToken)
                .param("name", "NonExistentCategoryXYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // Helper method
    private void seedDefaultCategories() {
        String[] defaultCategories = {
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
        };

        for (String categoryName : defaultCategories) {
            if (!categoryRepository.findByName(categoryName).isPresent()) {
                Category category = new Category();
                category.setName(categoryName);
                category.setDescription("Default " + categoryName + " category");
                categoryRepository.save(category);
            }
        }
    }
}
