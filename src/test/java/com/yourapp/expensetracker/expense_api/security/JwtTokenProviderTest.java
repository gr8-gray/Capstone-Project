package com.yourapp.expensetracker.expense_api.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider
 * Tests JWT token generation, validation, and extraction
 * @author Eric Gray - Backend Developer
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "REDACTED";
    private static final long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", TEST_EXPIRATION);
    }

    @Test
    void shouldGenerateTokenFromAuthentication() {
        // Given: Create proper UserDetails wrapper
        org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,  // UserDetails object, not String
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // When
        String token = jwtTokenProvider.generateToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void shouldGenerateTokenFromUsername() {
        // Given
        String username = "testuser";

        // When
        String token = jwtTokenProvider.generateTokenFromUsername(username);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        String username = "testuser";
        String token = jwtTokenProvider.generateTokenFromUsername(username);

        // When
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void shouldValidateValidToken() {
        // Given
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "notavalidjwttoken";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectNullToken() {
        // Given
        String nullToken = null;

        // When
        boolean isValid = jwtTokenProvider.validateToken(nullToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectTokenWithWrongSignature() {
        // Given: Generate token with different secret
        JwtTokenProvider differentProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(differentProvider, "jwtSecret", "differentSecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmSecureKey2024");
        ReflectionTestUtils.setField(differentProvider, "jwtExpirationMs", TEST_EXPIRATION);
        
        String tokenWithDifferentSecret = differentProvider.generateTokenFromUsername("testuser");

        // When: validateToken catches SignatureException and returns false
        boolean isValid = jwtTokenProvider.validateToken(tokenWithDifferentSecret);

        // Then: Token with wrong signature should be invalid
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        // Given: Generate token with very short expiration
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtExpirationMs", 1L); // 1 millisecond
        
        String token = shortExpirationProvider.generateTokenFromUsername("testuser");

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldGenerateUniqueTokensForDifferentUsers() {
        // Given
        String user1 = "user1";
        String user2 = "user2";

        // When
        String token1 = jwtTokenProvider.generateTokenFromUsername(user1);
        String token2 = jwtTokenProvider.generateTokenFromUsername(user2);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtTokenProvider.getUsernameFromToken(token1)).isEqualTo(user1);
        assertThat(jwtTokenProvider.getUsernameFromToken(token2)).isEqualTo(user2);
    }

    @Test
    void shouldGenerateDifferentTokensForSameUserAtDifferentTimes() {
        // Given
        String username = "testuser";

        // When
        String token1 = jwtTokenProvider.generateTokenFromUsername(username);
        
        // Wait 1 second (JWT 'iat' claim has second precision)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtTokenProvider.generateTokenFromUsername(username);

        // Then: Tokens should be different (due to different issued-at times)
        assertThat(token1).isNotEqualTo(token2);
        
        // But both should extract same username
        assertThat(jwtTokenProvider.getUsernameFromToken(token1)).isEqualTo(username);
        assertThat(jwtTokenProvider.getUsernameFromToken(token2)).isEqualTo(username);
    }

    @Test
    void shouldHandleUsernamesWithSpecialCharacters() {
        // Given
        String username = "test.user@example";

        // When
        String token = jwtTokenProvider.generateTokenFromUsername(username);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void shouldHandleLongUsernames() {
        // Given
        String longUsername = "a".repeat(100);

        // When
        String token = jwtTokenProvider.generateTokenFromUsername(longUsername);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(longUsername);
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenExtractingUsernameFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.getUsernameFromToken(invalidToken))
            .isInstanceOf(Exception.class);
    }

    @Test
    void shouldGenerateTokenWithCorrectStructure() {
        // Given
        String username = "testuser";

        // When
        String token = jwtTokenProvider.generateTokenFromUsername(username);

        // Then: JWT should have header.payload.signature structure
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        assertThat(parts[0]).isNotEmpty(); // header
        assertThat(parts[1]).isNotEmpty(); // payload
        assertThat(parts[2]).isNotEmpty(); // signature
    }
}
