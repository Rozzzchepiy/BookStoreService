package com.epam.rd.autocode.spring.project.service.impl;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expiration = 1000 * 60 * 60;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", expiration);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("testUser", username);
    }

    @Test
    void generateToken_ShouldGenerateValidToken() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void generateToken_WithExtraClaims_ShouldIncludeClaims() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("id", 123);

        String token = jwtService.generateToken(extraClaims, userDetails);

        Integer id = jwtService.extractClaim(token, claims -> claims.get("id", Integer.class));
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        assertEquals(123, id);
        assertEquals("ADMIN", role);
        assertEquals("testUser", jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForValidToken() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForDifferentUsername() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        UserDetails otherUser = new User("otherUser", "password", Collections.emptyList());

        boolean isValid = jwtService.isTokenValid(token, otherUser);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ShouldThrowException_ForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L); // -1 second

        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void extractClaim_ShouldReturnExpirationDate() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        Date expirationDate = jwtService.extractClaim(token, Claims::getExpiration);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }
}