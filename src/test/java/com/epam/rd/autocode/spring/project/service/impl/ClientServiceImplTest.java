package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.criteria.ClientSearchRequest;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.ClientProfile;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(clientService, "lockTimeDuration", 30L);
        ReflectionTestUtils.setField(clientService, "maxFailedAttempts", 3);
    }

    @Test
    void increaseFailedAttempts_ShouldIncrement_WhenBelowMax() {
        User user = new User();
        user.setFailedAttempt(0);

        clientService.increaseFailedAttempts(user);

        assertEquals(1, user.getFailedAttempt());
        assertNull(user.getLockTime());
        verify(userRepository).save(user);
    }

    @Test
    void increaseFailedAttempts_ShouldLock_WhenReachingMax() {
        User user = new User();
        user.setFailedAttempt(2);

        clientService.increaseFailedAttempts(user);

        assertEquals(3, user.getFailedAttempt());
        assertNotNull(user.getLockTime());
        verify(userRepository).save(user);
    }

    @Test
    void resetFailedAttempts_ShouldReset_WhenUserExists() {
        String email = "test@test.com";
        User user = new User();
        user.setFailedAttempt(5);
        user.setLockTime(LocalDateTime.now());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        clientService.resetFailedAttempts(email);

        assertEquals(0, user.getFailedAttempt());
        assertNull(user.getLockTime());
        verify(userRepository).save(user);
    }

    @Test
    void resetFailedAttempts_ShouldDoNothing_WhenUserNotFound() {
        String email = "unknown@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        clientService.resetFailedAttempts(email);

        verify(userRepository, never()).save(any());
    }

    @Test
    void lock_ShouldSetLockTime() {
        User user = new User();
        clientService.lock(user);
        assertNotNull(user.getLockTime());
    }

    @Test
    void unlockWhenTimeExpired_ShouldReturnTrue_WhenLockTimeIsNull() {
        User user = new User();
        user.setLockTime(null);

        boolean result = clientService.unlockWhenTimeExpired(user);

        assertTrue(result);
        verify(userRepository, never()).save(user);
    }

    @Test
    void unlockWhenTimeExpired_ShouldReturnTrue_WhenTimeExpired() {
        User user = new User();
        user.setLockTime(LocalDateTime.now().minusMinutes(1));

        boolean result = clientService.unlockWhenTimeExpired(user);

        assertTrue(result);
        assertNull(user.getLockTime());
        assertEquals(0, user.getFailedAttempt());
        verify(userRepository).save(user);
    }

    @Test
    void unlockWhenTimeExpired_ShouldReturnFalse_WhenTimeNotExpired() {
        User user = new User();
        user.setLockTime(LocalDateTime.now().plusMinutes(10));

        boolean result = clientService.unlockWhenTimeExpired(user);

        assertFalse(result);
        verify(userRepository, never()).save(user);
    }

    @Test
    void blockClient_ShouldBlockUser() {
        Long id = 1L;
        User user = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        clientService.blockClient(id);

        assertTrue(user.isBlocked());
        verify(userRepository).save(user);
    }

    @Test
    void unblockClient_ShouldUnblockUser() {
        Long id = 1L;
        User user = new User();
        user.setBlocked(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        clientService.unblockClient(id);

        assertFalse(user.isBlocked());
        verify(userRepository).save(user);
    }

    @Test
    void blockClient_ShouldThrowNotFound() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clientService.blockClient(id));
    }

    @Test
    void getAllClients_WithKeyword_ShouldReturnFilteredPage() {
        ClientSearchRequest request = mock(ClientSearchRequest.class);
        Pageable pageable = Pageable.unpaged();
        when(request.getKeyword()).thenReturn("John");
        when(request.getPageable()).thenReturn(pageable);

        User user = new User();
        user.setClientProfile(new ClientProfile());
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAllByRolesContainingAndKeyword(Role.CLIENT, "John", pageable))
                .thenReturn(page);
        when(modelMapper.map(user, ClientDTO.class)).thenReturn(new ClientDTO());

        Page<ClientDTO> result = clientService.getAllClients(request);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAllByRolesContainingAndKeyword(Role.CLIENT, "John", pageable);
    }

    @Test
    void getAllClients_WithoutKeyword_ShouldReturnAllPage() {
        ClientSearchRequest request = mock(ClientSearchRequest.class);
        Pageable pageable = Pageable.unpaged();
        when(request.getKeyword()).thenReturn(null);
        when(request.getPageable()).thenReturn(pageable);

        User user = new User();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAllByRolesContaining(Role.CLIENT, pageable))
                .thenReturn(page);
        when(modelMapper.map(user, ClientDTO.class)).thenReturn(new ClientDTO());

        Page<ClientDTO> result = clientService.getAllClients(request);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAllByRolesContaining(Role.CLIENT, pageable);
    }

    @Test
    void getClientById_ShouldReturnClient() {
        Long id = 1L;
        User user = new User();
        user.setRoles(Set.of(Role.CLIENT));
        user.setClientProfile(new ClientProfile());
        user.getClientProfile().setBalance(BigDecimal.TEN);
        ClientDTO dto = new ClientDTO();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, ClientDTO.class)).thenReturn(dto);

        ClientDTO result = clientService.getClientById(id);

        assertNotNull(result);
        assertNull(result.getPassword());
        assertEquals(BigDecimal.TEN, result.getBalance());
    }

    @Test
    void getClientById_ShouldThrow_WhenWrongRole() {
        Long id = 1L;
        User user = new User();
        user.setRoles(Set.of(Role.ADMIN));
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class, () -> clientService.getClientById(id));
    }

    @Test
    void getClientById_ShouldThrow_WhenNotFound() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clientService.getClientById(id));
    }

    @Test
    void getClientByEmail_ShouldReturnClient() {
        String email = "test@test.com";
        User user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, ClientDTO.class)).thenReturn(new ClientDTO());

        ClientDTO result = clientService.getClientByEmail(email);
        assertNotNull(result);
    }

    @Test
    void getClientByEmail_ShouldThrow_WhenNotFound() {
        String email = "test@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clientService.getClientByEmail(email));
    }

    @Test
    void updateClient_ShouldUpdateFields() {
        Long id = 1L;
        User user = new User();
        user.setRoles(Set.of(Role.CLIENT));
        user.setEmail("old@test.com");
        user.setClientProfile(new ClientProfile());

        ClientDTO dto = new ClientDTO();
        dto.setEmail("new@test.com");
        dto.setName("New Name");
        dto.setPassword("newPass");
        dto.setBalance(BigDecimal.valueOf(100));

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newPass")).thenReturn("encodedPass");
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, ClientDTO.class)).thenReturn(dto);

        ClientDTO result = clientService.updateClient(id, dto);

        assertEquals("new@test.com", user.getEmail());
        assertEquals("New Name", user.getName());
        assertEquals("encodedPass", user.getPassword());
        assertEquals(BigDecimal.valueOf(100), user.getClientProfile().getBalance());
        assertNotNull(result);
    }

    @Test
    void updateClient_ShouldThrow_WhenEmailExists() {
        Long id = 1L;
        User user = new User();
        user.setRoles(Set.of(Role.CLIENT));
        user.setEmail("old@test.com");

        ClientDTO dto = new ClientDTO();
        dto.setEmail("exists@test.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(AlreadyExistException.class, () -> clientService.updateClient(id, dto));
    }

    @Test
    void deleteClient_ShouldDelete() {
        Long id = 1L;
        when(userRepository.existsById(id)).thenReturn(true);
        clientService.deleteClient(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteClient_ShouldThrow_WhenNotFound() {
        Long id = 1L;
        when(userRepository.existsById(id)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> clientService.deleteClient(id));
    }

    @Test
    void addClient_ShouldCreateNewClient() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("new@test.com");
        dto.setPassword("pass");
        dto.setBalance(BigDecimal.TEN);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(User.class), eq(ClientDTO.class))).thenReturn(dto);

        ClientDTO result = clientService.addClient(dto);

        verify(userRepository).save(any(User.class));
        assertNotNull(result);
    }

    @Test
    void addClient_ShouldThrow_WhenEmailExists() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("exist@test.com");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(AlreadyExistException.class, () -> clientService.addClient(dto));
    }

    @Test
    void topUpBalance_ShouldIncreaseBalance() {
        String email = "test@test.com";
        BigDecimal amount = BigDecimal.TEN;
        User user = new User();
        user.setClientProfile(new ClientProfile());
        user.getClientProfile().setBalance(BigDecimal.ZERO);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        clientService.topUpBalance(email, amount);

        assertEquals(BigDecimal.TEN, user.getClientProfile().getBalance());
        verify(userRepository).save(user);
    }

    @Test
    void topUpBalance_ShouldThrow_WhenAmountInvalid() {
        assertThrows(IllegalArgumentException.class, () -> clientService.topUpBalance("email", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> clientService.topUpBalance("email", BigDecimal.valueOf(-1)));
        assertThrows(IllegalArgumentException.class, () -> clientService.topUpBalance("email", null));
    }

    @Test
    void topUpBalance_ShouldThrow_WhenUserNotFound() {
        String email = "unknown";
        BigDecimal amount = BigDecimal.TEN;
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clientService.topUpBalance(email, amount));
    }
}