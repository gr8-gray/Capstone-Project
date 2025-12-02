package com.yourapp.expensetracker.expense_api;

import com.yourapp.expensetracker.expense_api.model.User;
import com.yourapp.expensetracker.expense_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected User testUser;
    protected String testUsername = "testuser";
    protected String testPassword = "password123";

    @BeforeEach
    public void setUpBaseTest() {
        // Check if test user already exists (to prevent duplicate user creation in transaction rollback scenarios)
        testUser = userRepository.findByUsername(testUsername).orElse(null);
        
        if (testUser == null) {
            // Create test user only if it doesn't exist
            testUser = new User();
            testUser.setUsername(testUsername);
            testUser.setEmail("testuser@example.com");
            testUser.setPasswordHash(passwordEncoder.encode(testPassword));
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setRole("USER");
            testUser.setActive(true);
            testUser.setCreatedAt(LocalDateTime.now());
            testUser = userRepository.save(testUser);
            userRepository.flush();  // Force immediate persistence
        }

        // Set up Spring Security context using username
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                testUser.getUsername(), 
                null, 
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    protected User createAdditionalUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setFirstName(username);
        user.setLastName("");
        user.setRole("USER");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        userRepository.flush();
        return savedUser;
    }
}
