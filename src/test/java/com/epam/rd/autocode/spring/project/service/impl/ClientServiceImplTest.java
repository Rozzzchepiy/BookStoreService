package com.epam.rd.autocode.spring.project.service.impl;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClientServiceImpl clientService;

    private User clientUser;
    private ClientDTO clientDTO;

    @BeforeEach
    void setUp() {
        clientUser = new User();
        clientUser.setId(1L);
        clientUser.setEmail("client@test.com");
        clientUser.setName("Client Name");
        clientUser.setPassword("encoded_password");
        clientUser.setRoles(new HashSet<>(Collections.singletonList(Role.CLIENT)));

        ClientProfile profile = new ClientProfile();
        profile.setId(1L);
        profile.setBalance(new BigDecimal("100.00"));
        profile.setUser(clientUser);

        clientUser.setClientProfile(profile);

        clientDTO = new ClientDTO();
        clientDTO.setId(1L);
        clientDTO.setEmail("client@test.com");
        clientDTO.setName("Client Name");
        clientDTO.setPassword("plain_password");
        clientDTO.setBalance(new BigDecimal("100.00"));
    }


    @Test
    void getAllClients_Success() {
        when(userRepository.findAllByRolesContaining(Role.CLIENT))
                .thenReturn(List.of(clientUser));

        List<ClientDTO> result = clientService.getAllClients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("client@test.com");
        assertThat(result.get(0).getBalance()).isEqualByComparingTo("100.00");
    }


    @Test
    void getClientById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));

        ClientDTO result = clientService.getClientById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Client Name");
    }

    @Test
    void getClientById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Client not found");
    }

    @Test
    void getClientById_NotAClientRole() {
        User employee = new User();
        employee.setId(2L);
        employee.setRoles(Set.of(Role.EMPLOYEE));

        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> clientService.getClientById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("This user is not a client");
    }


    @Test
    void getClientByEmail_Success() {
        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(clientUser));

        ClientDTO result = clientService.getClientByEmail("client@test.com");

        assertThat(result.getEmail()).isEqualTo("client@test.com");
    }

    @Test
    void getClientByEmail_NotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientByEmail("unknown@test.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Client not found");
    }


    @Test
    void addClient_Success() {
        when(userRepository.findByEmail(clientDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain_password")).thenReturn("encoded_secret");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            u.getClientProfile().setId(10L);
            return u;
        });

        ClientDTO result = clientService.addClient(clientDTO);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getBalance()).isEqualByComparingTo("100.00");

        verify(passwordEncoder).encode("plain_password");
        verify(userRepository).save(argThat(u ->
                u.getRoles().contains(Role.CLIENT) &&
                        u.getClientProfile().getBalance().compareTo(new BigDecimal("100.00")) == 0
        ));
    }

    @Test
    void addClient_AlreadyExists() {
        when(userRepository.findByEmail(clientDTO.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> clientService.addClient(clientDTO))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("Client already exists");

        verify(userRepository, never()).save(any());
    }


    @Test
    void updateClient_Success_WithPasswordAndBalance() {
        clientDTO.setName("New Name");
        clientDTO.setPassword("new_pass");
        clientDTO.setBalance(new BigDecimal("500.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));
        when(passwordEncoder.encode("new_pass")).thenReturn("encoded_new_pass");
        when(userRepository.save(any(User.class))).thenReturn(clientUser);

        ClientDTO result = clientService.updateClient(1L, clientDTO);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getBalance()).isEqualByComparingTo("500.00");

        assertThat(clientUser.getName()).isEqualTo("New Name");
        assertThat(clientUser.getPassword()).isEqualTo("encoded_new_pass");
        assertThat(clientUser.getClientProfile().getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    void updateClient_Success_EmailChange_Unique() {
        clientDTO.setEmail("new@email.com");
        clientDTO.setPassword(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));
        when(userRepository.findByEmail("new@email.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(clientUser);

        clientService.updateClient(1L, clientDTO);

        assertThat(clientUser.getEmail()).isEqualTo("new@email.com");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateClient_Fail_EmailExists() {
        clientDTO.setEmail("existing@email.com");
        User existingUser = new User();
        existingUser.setId(5L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));
        when(userRepository.findByEmail("existing@email.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> clientService.updateClient(1L, clientDTO))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("Client with this email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateClient_Fail_NotAClient() {
        User admin = new User();
        admin.setId(1L);
        admin.setRoles(Set.of(Role.ADMIN));

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> clientService.updateClient(1L, clientDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("This user is not a client");
    }


    @Test
    void deleteClient_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));

        clientService.deleteClient(1L);

        verify(userRepository).delete(clientUser);
    }

    @Test
    void deleteClient_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.deleteClient(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Client not found");
    }


    @Test
    void topUpBalance_Success() {
        BigDecimal topUpAmount = new BigDecimal("50.00");

        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(clientUser));

        clientService.topUpBalance("client@test.com", topUpAmount);

        assertThat(clientUser.getClientProfile().getBalance()).isEqualByComparingTo("150.00");
        verify(userRepository).save(clientUser);
    }

    @Test
    void topUpBalance_UserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.topUpBalance("unknown@test.com", BigDecimal.TEN))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void topUpBalance_NegativeAmount() {
        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(clientUser));

        BigDecimal negativeAmount = new BigDecimal("-10.00");

        assertThatThrownBy(() -> clientService.topUpBalance("client@test.com", negativeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сума поповнення має бути додатною");

        verify(userRepository, never()).save(any());
    }

    @Test
    void topUpBalance_ZeroAmount() {
        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(clientUser));

        BigDecimal zero = BigDecimal.ZERO;

        assertThatThrownBy(() -> clientService.topUpBalance("client@test.com", zero))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сума поповнення має бути додатною");
    }
}