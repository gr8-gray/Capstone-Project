package com.yourapp.expensetracker.expense_api.controller;

import com.yourapp.expensetracker.expense_api.dto.AuthResponse;
import com.yourapp.expensetracker.expense_api.dto.LoginRequest;
import com.yourapp.expensetracker.expense_api.dto.RegisterRequest;
import com.yourapp.expensetracker.expense_api.dto.UserDTO;
import com.yourapp.expensetracker.expense_api.model.User;
import com.yourapp.expensetracker.expense_api.service.AuthService;
import com.yourapp.expensetracker.expense_api.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for authentication operations
 * Handles user registration and login
 * @author Eric Gray - Backend Developer
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration attempt for username: {}, email: {}", request.getUsername(), request.getEmail());
        try {
            AuthResponse response = authService.register(request);
            logger.info("User registered successfully: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed - validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Registration failed for username: {} - {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed. Please try again later."));
        }
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt for user: {}", request.getUsernameOrEmail());
        try {
            AuthResponse response = authService.login(request);
            logger.info("User logged in successfully: {}", request.getUsernameOrEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.warn("Login failed for user: {} - {}", request.getUsernameOrEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username/email or password"));
        }
    }

    /**
     * Get current user profile
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        logger.debug("Fetching current user profile");
        try {
            User user = authService.getCurrentUser();
            UserDTO userDTO = userService.convertToDTO(user);
            logger.debug("Current user profile retrieved: {}", user.getUsername());
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            logger.warn("Failed to get current user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }
    }

    /**
     * Logout (client-side token removal)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT is stateless, so logout is handled client-side by removing the token
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Check if username exists
     * GET /api/auth/check-username?username=...
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userService.getUserByUsername(username).isPresent();
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Check if email exists
     * GET /api/auth/check-email?email=...
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userService.getUserByEmail(email).isPresent();
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
