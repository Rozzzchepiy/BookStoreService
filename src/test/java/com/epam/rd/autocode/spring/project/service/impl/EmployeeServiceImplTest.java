package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.EmployeeProfile;
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

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private User employeeUser;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        employeeUser = new User();
        employeeUser.setId(1L);
        employeeUser.setEmail("employee@test.com");
        employeeUser.setName("John Doe");
        employeeUser.setPassword("encoded_password");
        employeeUser.setRoles(new HashSet<>(Collections.singletonList(Role.EMPLOYEE)));

        EmployeeProfile profile = new EmployeeProfile();
        profile.setId(1L);
        profile.setPhone("1234567890");
        profile.setBirthDate(LocalDate.of(1990, 1, 1));
        profile.setUser(employeeUser);

        employeeUser.setEmployeeProfile(profile);

        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(1L);
        employeeDTO.setEmail("employee@test.com");
        employeeDTO.setName("John Doe");
        employeeDTO.setPassword("plain_password");
        employeeDTO.setPhone("1234567890");
        employeeDTO.setBirthDate(LocalDate.of(1990, 1, 1));
    }


    @Test
    void getAllEmployees_Success() {
        when(userRepository.findAllByRolesContaining(Role.EMPLOYEE))
                .thenReturn(List.of(employeeUser));

        List<EmployeeDTO> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo(employeeUser.getEmail());
        assertThat(result.get(0).getPassword()).isNull();
    }

    @Test
    void getEmployeeById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employeeUser));

        EmployeeDTO result = employeeService.getEmployeeById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPhone()).isEqualTo("1234567890");
    }

    @Test
    void getEmployeeById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Employee not found");
    }

    @Test
    void getEmployeeById_UserExistsButNotEmployee() {
        User clientUser = new User();
        clientUser.setId(2L);
        clientUser.setRoles(Set.of(Role.CLIENT));

        when(userRepository.findById(2L)).thenReturn(Optional.of(clientUser));

        assertThatThrownBy(() -> employeeService.getEmployeeById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("This user is not an employee");
    }


    @Test
    void addEmployee_Success() {
        when(userRepository.findByEmail(employeeDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(employeeDTO.getPassword())).thenReturn("encoded_secret");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(10L);
            savedUser.getEmployeeProfile().setId(10L);
            return savedUser;
        });

        EmployeeDTO result = employeeService.addEmployee(employeeDTO);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo(employeeDTO.getName());

        verify(passwordEncoder).encode("plain_password");
        verify(userRepository).save(argThat(user ->
                user.getRoles().contains(Role.EMPLOYEE) &&
                        user.getEmployeeProfile().getPhone().equals(employeeDTO.getPhone())
        ));
    }

    @Test
    void addEmployee_EmailAlreadyExists() {
        when(userRepository.findByEmail(employeeDTO.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> employeeService.addEmployee(employeeDTO))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("Employee with this email already exists");

        verify(userRepository, never()).save(any());
    }


    @Test
    void updateEmployee_Success_NoEmailChange_UpdatePassword() {
        employeeDTO.setName("New Name");
        employeeDTO.setPhone("0987654321");
        employeeDTO.setPassword("new_password");

        when(userRepository.findById(1L)).thenReturn(Optional.of(employeeUser));
        when(passwordEncoder.encode("new_password")).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(employeeUser);

        EmployeeDTO result = employeeService.updateEmployee(1L, employeeDTO);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPhone()).isEqualTo("0987654321");

        assertThat(employeeUser.getName()).isEqualTo("New Name");
        assertThat(employeeUser.getPassword()).isEqualTo("new_encoded_password");
        assertThat(employeeUser.getEmployeeProfile().getPhone()).isEqualTo("0987654321");
    }

    @Test
    void updateEmployee_Success_EmailChange_Unique() {
        employeeDTO.setEmail("new@test.com");
        employeeDTO.setPassword(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(employeeUser));
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(employeeUser);

        employeeService.updateEmployee(1L, employeeDTO);

        assertThat(employeeUser.getEmail()).isEqualTo("new@test.com");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateEmployee_Fail_EmailChange_Exists() {
        employeeDTO.setEmail("busy@test.com");
        User anotherUser = new User();
        anotherUser.setId(5L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(employeeUser));
        when(userRepository.findByEmail("busy@test.com")).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, employeeDTO))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("Employee with this email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateEmployee_Fail_NotAnEmployee() {
        User clientUser = new User();
        clientUser.setId(1L);
        clientUser.setRoles(Set.of(Role.CLIENT));

        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, employeeDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("This user is not an employee");
    }

    @Test
    void updateEmployee_Fail_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, employeeDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Employee not found");
    }


    @Test
    void deleteEmployee_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employeeUser));

        employeeService.deleteEmployee(1L);

        verify(userRepository).delete(employeeUser);
    }

    @Test
    void deleteEmployee_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Employee not found");

        verify(userRepository, never()).delete(any());
    }
}