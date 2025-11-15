package com.yourapp.expensetracker.expense_api;

import com.yourapp.expensetracker.expense_api.dto.LoginRequest;
import com.yourapp.expensetracker.expense_api.dto.RegisterRequest;
import com.yourapp.expensetracker.expense_api.model.User;
import com.yourapp.expensetracker.expense_api.repository.UserRepository;
import com.yourapp.expensetracker.expense_api.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for authentication functionality
 * @author Eric Gray - Backend Developer
 */
@SpringBootTest
@ActiveProfiles("test")
public class AuthenticationIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    public void testUserCreationAndPasswordEncoding() {
        // Create a test user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole("USER");
        user.setEnabled(true);

        // Save user
        User savedUser = userRepository.save(user);

        // Verify user was saved
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertNotEquals("password123", savedUser.getPasswordHash()); // Should be hashed
        assertTrue(passwordEncoder.matches("password123", savedUser.getPasswordHash()));
    }

    @Test
    public void testJwtTokenGeneration() {
        // Generate token
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        // Verify token is not null
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Validate token
        assertTrue(jwtTokenProvider.validateToken(token));

        // Extract username from token
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    public void testRegisterRequestValidation() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");

        assertNotNull(request.getUsername());
        assertNotNull(request.getEmail());
        assertNotNull(request.getPassword());
    }

    @Test
    public void testLoginRequestValidation() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("password123");

        assertNotNull(request.getUsernameOrEmail());
        assertNotNull(request.getPassword());
    }
}
