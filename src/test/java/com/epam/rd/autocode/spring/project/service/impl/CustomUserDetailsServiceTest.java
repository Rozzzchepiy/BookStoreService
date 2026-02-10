package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExistsAndActive() {
        String email = "test@test.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setRoles(Set.of(Role.CLIENT));
        user.setBlocked(false);
        user.setLockTime(null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")));
        assertTrue(userDetails.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        String email = "unknown@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(email));
    }

    @Test
    void loadUserByUsername_ShouldReturnLockedAccount_WhenUserIsBlocked() {
        String email = "blocked@test.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setRoles(Set.of(Role.CLIENT));
        user.setBlocked(true);
        user.setLockTime(null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_ShouldReturnLockedAccount_WhenLockTimeIsFuture() {
        String email = "locked@test.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setRoles(Set.of(Role.CLIENT));
        user.setBlocked(false);
        user.setLockTime(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_ShouldUnlockAndReturnActive_WhenLockTimeIsPast() {
        String email = "expired@test.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setRoles(Set.of(Role.CLIENT));
        user.setBlocked(false);
        user.setFailedAttempt(5);
        user.setLockTime(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        assertNull(user.getLockTime());
        assertEquals(0, user.getFailedAttempt());
        assertTrue(userDetails.isAccountNonLocked());
        verify(userRepository).save(user);
    }
}