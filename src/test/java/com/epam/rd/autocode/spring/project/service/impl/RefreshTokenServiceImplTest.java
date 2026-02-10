package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.RefreshToken;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.RefreshTokenRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 60000L);
    }

    @Test
    void createRefreshToken_ShouldCreateNewToken_WhenNoneExists() {
        String email = "test@test.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(email);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertNotNull(result.getExpiryDate());
        assertEquals(user, result.getUser());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_ShouldUpdateExistingToken_WhenExists() {
        String email = "test@test.com";
        User user = new User();
        RefreshToken existingToken = new RefreshToken();
        existingToken.setUser(user);
        existingToken.setToken("old-token");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(existingToken)).thenReturn(existingToken);

        RefreshToken result = refreshTokenService.createRefreshToken(email);

        assertNotNull(result);
        assertNotEquals("old-token", result.getToken());
        assertNotNull(result.getExpiryDate());
        verify(refreshTokenRepository).save(existingToken);
    }

    @Test
    void createRefreshToken_ShouldThrowNotFound_WhenUserMissing() {
        String email = "unknown@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> refreshTokenService.createRefreshToken(email));
    }

    @Test
    void findByToken_ShouldReturnToken_WhenFound() {
        String tokenStr = "uuid-token";
        RefreshToken token = new RefreshToken();
        when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenStr);

        assertTrue(result.isPresent());
        assertEquals(token, result.get());
    }

    @Test
    void findByToken_ShouldReturnEmpty_WhenNotFound() {
        String tokenStr = "uuid-token";
        when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenStr);

        assertTrue(result.isEmpty());
    }

    @Test
    void verifyExpiration_ShouldReturnToken_WhenNotExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().plusMillis(10000));

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertEquals(token, result);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_ShouldThrowAndRemove_WhenExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusMillis(1000));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                refreshTokenService.verifyExpiration(token));

        assertEquals("Refresh token was expired. Please make a new signin request", exception.getMessage());
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void deleteByUserId_ShouldDelete_WhenUserExists() {
        Long userId = 1L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        refreshTokenService.deleteByUserId(userId);

        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void deleteByUserId_ShouldDoNothing_WhenUserNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        refreshTokenService.deleteByUserId(userId);

        verify(refreshTokenRepository, never()).deleteByUser(any());
    }
}