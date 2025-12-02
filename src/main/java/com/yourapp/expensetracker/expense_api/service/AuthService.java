package com.yourapp.expensetracker.expense_api.service;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import com.yourapp.expensetracker.expense_api.repository.UserRepository;
import com.yourapp.expensetracker.expense_api.model.User;

import com.yourapp.expensetracker.expense_api.dto.AuthResponse;
import com.yourapp.expensetracker.expense_api.dto.LoginRequest;
import com.yourapp.expensetracker.expense_api.dto.RegisterRequest;
import com.yourapp.expensetracker.expense_api.model.User;
import com.yourapp.expensetracker.expense_api.security.JwtTokenProvider;

/**
 * Service for authentication operations (login, register)
 * @author Eric Gray - Backend Developer
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserService userService, 
                      JwtTokenProvider jwtTokenProvider,
                      AuthenticationManager authenticationManager,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
                    request.getFullName(),
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
            throw e;
        }
    }

    /**
     * Login user
     */
    public AuthResponse login(LoginRequest request) {
    logger.info("Authenticating user: {}", request.getUsernameOrEmail());

    try {
        // Determine if login input is username or email
        String loginInput = request.getUsernameOrEmail();
        String usernameToUse;

        if (loginInput.contains("@")) {
            // Login using email
            usernameToUse = userService.getUserByEmail(loginInput)
                    .map(User::getUsername)
                    .orElseThrow(() -> new BadCredentialsException("Invalid email"));
        } else {
            // Login using username
            usernameToUse = loginInput;
        }

        logger.info("Authenticating using username: {}", usernameToUse);

        // Authenticate with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        usernameToUse,
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.debug("User authenticated successfully: {}", authentication.getName());

        // Fetch the authenticated user
        User user = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        // Update last login timestamp
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        logger.info("Login successful for user: {}", user.getUsername());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );

    } catch (BadCredentialsException e) {
        logger.warn("Invalid credentials for user: {}", request.getUsernameOrEmail());
        throw e;
    } catch (AuthenticationException e) {
        logger.warn("Authentication error for user {}: {}", request.getUsernameOrEmail(), e.getMessage());
        throw e;
    } catch (Exception e) {
        logger.error("Unexpected login error for user {}: {}", request.getUsernameOrEmail(), e.getMessage(), e);
        throw new RuntimeException("Login failed: " + e.getMessage(), e);
    }
}

    public void resetPassword(String email) {
        logger.info("Attempting to reset password for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String tempPassword = "Temp@1234";

        String hashed = passwordEncoder.encode(tempPassword);
        user.setPasswordHash(hashed);

        userRepository.save(user);

        logger.info("Password reset successfully for user ID {}", user.getId());
    }

    public void changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser(); // fetch logged-in user

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Save new hashed password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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
