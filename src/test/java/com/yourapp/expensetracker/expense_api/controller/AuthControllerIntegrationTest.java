package com.yourapp.expensetracker.expense_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourapp.expensetracker.expense_api.BaseIntegrationTest;
import com.yourapp.expensetracker.expense_api.dto.LoginRequest;
import com.yourapp.expensetracker.expense_api.dto.RegisterRequest;
import com.yourapp.expensetracker.expense_api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController
 * Tests registration, login, and authentication flows
 * @author Eric Gray - Backend Developer
 */
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Do NOT call super.setUpBaseTest() - auth tests need clean state
        // Auth tests create their own users as needed
    }

    @Test
    void shouldRegisterNewUser() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFullName("New User");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    void shouldRejectRegistrationWithDuplicateUsername() throws Exception {
        // Given: Existing user
        createTestUser("existinguser", "existing@example.com");

        // When: Try to register with same username
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("newemail@example.com");
        request.setPassword("password123");

        // Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("already exists with username")));
    }

    @Test
    void shouldRejectRegistrationWithDuplicateEmail() throws Exception {
        // Given: Existing user
        createTestUser("existinguser", "existing@example.com");

        // When: Try to register with same email
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newusername");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        // Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("already exists with email")));
    }

    @Test
    void shouldRejectRegistrationWithInvalidEmail() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("invalid-email");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRegistrationWithShortUsername() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab"); // Less than 3 characters
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRegistrationWithShortPassword() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("12345"); // Less than 6 characters

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        // Given: Registered user
        String uniqueUsername = "testuser_" + System.currentTimeMillis();
        createTestUser(uniqueUsername, "test@example.com");

        // When: Login with username
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail(uniqueUsername);
        request.setPassword("password123");

        // Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(uniqueUsername))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldLoginWithEmail() throws Exception {
        // Given: Registered user
        String uniqueUsername = "testuser_" + System.currentTimeMillis();
        String uniqueEmail = "test_" + System.currentTimeMillis() + "@example.com";
        createTestUser(uniqueUsername, uniqueEmail);

        // When: Login with email
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail(uniqueEmail);
        request.setPassword("password123");

        // Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(uniqueUsername));
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() throws Exception {
        // Given: Registered user
        String uniqueUsername = "testuser_" + System.currentTimeMillis();
        createTestUser(uniqueUsername, "test@example.com");

        // When: Login with wrong password
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail(uniqueUsername);
        request.setPassword("wrongpassword");

        // Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(containsString("Invalid username/email or password")));
    }

    @Test
    void shouldRejectLoginWithNonExistentUser() throws Exception {
        // Given: No user exists

        // When: Try to login
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("nonexistent");
        request.setPassword("password123");

        // Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHashPasswordOnRegistration() throws Exception {
        // Given
        String uniqueUsername = "testuser_" + System.currentTimeMillis();
        RegisterRequest request = new RegisterRequest();
        request.setUsername(uniqueUsername);
        request.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        // When: Register user
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then: Password should be hashed in database
        User user = userRepository.findByUsername(uniqueUsername).orElseThrow();
        assert !user.getPasswordHash().equals("password123");
        assert passwordEncoder.matches("password123", user.getPasswordHash());
    }

    @Test
    void shouldSetDefaultRoleForNewUser() throws Exception {
        // Given
        String uniqueUsername = "testuser_" + System.currentTimeMillis();
        RegisterRequest request = new RegisterRequest();
        request.setUsername(uniqueUsername);
        request.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        // When: Register user
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then: User should have USER role
        User user = userRepository.findByUsername(uniqueUsername).orElseThrow();
        assert user.getRole().equals("USER");
        assert user.isActive();
    }

    @Test
    void shouldRejectEmptyRegistrationFields() throws Exception {
        // Given: Empty request
        RegisterRequest request = new RegisterRequest();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEmptyLoginFields() throws Exception {
        // Given: Empty request
        LoginRequest request = new LoginRequest();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateLastLoginOnSuccessfulLogin() throws Exception {
        // Given: Registered user
        String uniqueUsername = "testuser_" + System.currentTimeMillis();
        User user = createTestUser(uniqueUsername, "test@example.com");
        assert user.getLastLogin() == null;

        // When: Login
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail(uniqueUsername);
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then: Last login should be updated
        User updatedUser = userRepository.findByUsername(uniqueUsername).orElseThrow();
        assert updatedUser.getLastLogin() != null;
    }

    @Test
    void shouldNotExposePasswordHashInResponse() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser_" + System.currentTimeMillis());
        request.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        // When: Register user
        String response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Then: Password hash should not be in response
        assert !response.contains("passwordHash");
        assert !response.contains("password123");
    }

    @Test
    void shouldAllowRegistrationOfMultipleUsers() throws Exception {
        // Given: Register first user
        RegisterRequest request1 = new RegisterRequest();
        request1.setUsername("user1");
        request1.setEmail("user1@example.com");
        request1.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // When: Register second user
        RegisterRequest request2 = new RegisterRequest();
        request2.setUsername("user2");
        request2.setEmail("user2@example.com");
        request2.setPassword("password123");

        // Then: Both users should exist
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // Verify both users exist in database
        assert userRepository.findByUsername("user1").isPresent();
        assert userRepository.findByUsername("user2").isPresent();
    }

    // Helper method
    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole("USER");
        user.setActive(true);
        return userRepository.save(user);
    }
}
