package com.yourapp.expensetracker.expense_api.service;

import com.yourapp.expensetracker.expense_api.dto.UserDTO;
import com.yourapp.expensetracker.expense_api.exception.DuplicateResourceException;
import com.yourapp.expensetracker.expense_api.exception.ResourceNotFoundException;
import com.yourapp.expensetracker.expense_api.model.User;
import com.yourapp.expensetracker.expense_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for user management operations
 * @author Eric Gray - Backend Developer
 * @author Michael Basye - Database Engineer (Added logging)
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user
     */
    public User createUser(String username, String email, String password, String fullName, String role) {
        logger.info("Attempting to create user with username: {}", username);
        
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            logger.warn("User creation failed: Username already exists: {}", username);
            throw new DuplicateResourceException("User", "username", username);
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            logger.warn("User creation failed: Email already exists: {}", email);
            throw new DuplicateResourceException("User", "email", email);
        }

        // Create user with hashed password
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role != null ? role : "USER");
        user.setFirstName(fullName);
        user.setLastName("");
        user.setActive(true);

        User savedUser = userRepository.save(user);
        logger.info("Successfully created user with ID: {} and username: {}", 
                   savedUser.getId(), savedUser.getUsername());
        return savedUser;
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Update user
     */
    public User updateUser(Long id, String username, String email, String role) {
        logger.debug("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User update failed: User not found with ID: {}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });

        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                logger.warn("User update failed: Username already exists: {}", username);
                throw new DuplicateResourceException("User", "username", username);
            }
            user.setUsername(username);
        }

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                logger.warn("User update failed: Email already exists: {}", email);
                throw new DuplicateResourceException("User", "email", email);
            }
            user.setEmail(email);
        }

        if (role != null) {
            user.setRole(role);
        }

        User updatedUser = userRepository.save(user);
        logger.info("Successfully updated user with ID: {}", id);
        return updatedUser;
    }

    /**
     * Update password
     */
    public void updatePassword(Long id, String newPassword) {
        logger.info("Updating password for user ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Password update failed: User not found with ID: {}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Successfully updated password for user ID: {}", id);
    }

    /**
     * Delete user
     */
    public void deleteUser(Long id) {
        logger.warn("Attempting to delete user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.error("User deletion failed: User not found with ID: {}", id);
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
        logger.info("Successfully deleted user with ID: {}", id);
    }

    /**
     * Convert User entity to UserDTO
     */
    public UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Convert list of User entities to DTOs
     */
    public List<UserDTO> convertToDTOList(List<User> users) {
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
