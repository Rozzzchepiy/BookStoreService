package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.model.PasswordResetToken;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.PasswordResetTokenRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    @Test
    void initiatePasswordReset_ShouldSaveTokenAndSendEmail_WhenUserExists() {
        String email = "test@example.com";
        String appUrl = "http://localhost:8080";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        passwordResetService.initiatePasswordReset(email, appUrl);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertNotNull(savedToken.getToken());
        assertEquals(user, savedToken.getUser());
        assertNotNull(savedToken.getExpiryDate());

        String expectedLink = appUrl + "/forgot-password/reset?token=" + savedToken.getToken();
        verify(emailService).sendEmail(eq(email), anyString(), contains(expectedLink));
    }

    @Test
    void initiatePasswordReset_ShouldDoNothing_WhenUserNotFound() {
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        passwordResetService.initiatePasswordReset(email, "http://localhost");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void validatePasswordResetToken_ShouldReturnTrue_WhenTokenValid() {
        String tokenString = "valid-token";
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenString);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        boolean result = passwordResetService.validatePasswordResetToken(tokenString);

        assertTrue(result);
    }

    @Test
    void validatePasswordResetToken_ShouldReturnFalse_WhenTokenNotFound() {
        String tokenString = "invalid-token";
        when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.empty());

        boolean result = passwordResetService.validatePasswordResetToken(tokenString);

        assertFalse(result);
    }

    @Test
    void validatePasswordResetToken_ShouldReturnFalse_WhenTokenExpired() {
        String tokenString = "expired-token";
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenString);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(10));

        when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        boolean result = passwordResetService.validatePasswordResetToken(tokenString);

        assertFalse(result);
    }

    @Test
    void updatePassword_ShouldUpdatePassword_WhenTokenValid() {
        String tokenString = "valid-token";
        String newPassword = "newPassword123";
        String encodedPassword = "encodedPassword";

        User user = new User();
        user.setEmail("user@example.com");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenString);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        passwordResetService.updatePassword(tokenString, newPassword);

        verify(userRepository).save(user);
        assertEquals(encodedPassword, user.getPassword());
        verify(tokenRepository).delete(token);
    }

    @Test
    void updatePassword_ShouldThrowException_WhenTokenNotFound() {
        String tokenString = "unknown";
        when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                passwordResetService.updatePassword(tokenString, "pass"));
    }

    @Test
    void updatePassword_ShouldThrowException_WhenTokenExpired() {
        String tokenString = "expired";
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () ->
                passwordResetService.updatePassword(tokenString, "pass"));
        verify(userRepository, never()).save(any());
    }
}