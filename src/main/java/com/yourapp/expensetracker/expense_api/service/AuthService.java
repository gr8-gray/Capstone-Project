package com.yourapp.expensetracker.expense_api.service;

import com.yourapp.expensetracker.expense_api.dto.AuthResponse;
import com.yourapp.expensetracker.expense_api.dto.LoginRequest;
import com.yourapp.expensetracker.expense_api.dto.RegisterRequest;
import com.yourapp.expensetracker.expense_api.model.User;
import com.yourapp.expensetracker.expense_api.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations (login, register)
 * @author Eric Gray - Backend Developer
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserService userService, 
                      JwtTokenProvider jwtTokenProvider,
                      AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user: {}", request.getUsername());
        
        try {
            // Create user
            User user = userService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    "USER"
            );

            logger.debug("User created with ID: {}", user.getId());

            // Generate JWT token
            String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

            logger.info("Registration completed successfully for user: {}", user.getUsername());

            // Return authentication response
            return new AuthResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed for user {}: {}", request.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during registration for user {}: {}", request.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    /**
     * Login user
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("Authenticating user: {}", request.getUsernameOrEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("User authenticated successfully: {}", authentication.getName());

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);

            // Get user details
            User user = userService.getUserByUsername(authentication.getName())
                    .orElseThrow(() -> {
                        logger.error("User not found after successful authentication: {}", authentication.getName());
                        return new RuntimeException("User not found");
                    });

            logger.info("Login successful for user: {}", user.getUsername());

            // Return authentication response
            return new AuthResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
            );
        } catch (BadCredentialsException e) {
            logger.warn("Login failed - invalid credentials for user: {}", request.getUsernameOrEmail());
            throw e;
        } catch (AuthenticationException e) {
            logger.warn("Login failed - authentication error for user {}: {}", request.getUsernameOrEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user {}: {}", request.getUsernameOrEmail(), e.getMessage(), e);
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get currently authenticated user from security context
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String username = authentication.getName();
        return userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Get current user ID
     */
    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
