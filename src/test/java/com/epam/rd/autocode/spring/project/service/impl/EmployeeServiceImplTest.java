package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.criteria.EmployeeSearchRequest;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.EmployeeProfile;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void getAllEmployees_WithKeyword_ShouldReturnFilteredPage() {
        String keyword = "John";
        EmployeeSearchRequest request = mock(EmployeeSearchRequest.class);
        Pageable pageable = Pageable.unpaged();

        when(request.getKeyword()).thenReturn(keyword);
        when(request.getPageable()).thenReturn(pageable);

        User user = new User();
        user.setEmployeeProfile(new EmployeeProfile());
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAllByRolesContainingAndKeyword(Role.EMPLOYEE, keyword, pageable))
                .thenReturn(page);
        when(modelMapper.map(user, EmployeeDTO.class)).thenReturn(new EmployeeDTO());

        Page<EmployeeDTO> result = employeeService.getAllEmployees(request);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAllByRolesContainingAndKeyword(Role.EMPLOYEE, keyword, pageable);
    }

    @Test
    void getAllEmployees_WithoutKeyword_ShouldReturnAllPage() {
        EmployeeSearchRequest request = mock(EmployeeSearchRequest.class);
        Pageable pageable = Pageable.unpaged();

        when(request.getKeyword()).thenReturn(null);
        when(request.getPageable()).thenReturn(pageable);

        User user = new User();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAllByRolesContaining(Role.EMPLOYEE, pageable))
                .thenReturn(page);
        when(modelMapper.map(user, EmployeeDTO.class)).thenReturn(new EmployeeDTO());

        Page<EmployeeDTO> result = employeeService.getAllEmployees(request);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAllByRolesContaining(Role.EMPLOYEE, pageable);
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee() {
        Long id = 1L;
        User user = new User();
        user.setRoles(Set.of(Role.EMPLOYEE));
        user.setEmployeeProfile(new EmployeeProfile());
        user.getEmployeeProfile().setPhone("123");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, EmployeeDTO.class)).thenReturn(new EmployeeDTO());

        EmployeeDTO result = employeeService.getEmployeeById(id);

        assertNotNull(result);
        assertNull(result.getPassword());
    }

    @Test
    void getEmployeeById_ShouldThrow_WhenNotEmployee() {
        Long id = 1L;
        User user = new User();
        user.setRoles(Set.of(Role.CLIENT));

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class, () -> employeeService.getEmployeeById(id));
    }

    @Test
    void getEmployeeById_ShouldThrow_WhenNotFound() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.getEmployeeById(id));
    }

    @Test
    void updateEmployee_ShouldUpdateAllFields() {
        Long id = 1L;
        User user = new User();
        user.setRoles(Set.of(Role.EMPLOYEE));
        user.setEmail("old@test.com");
        user.setEmployeeProfile(new EmployeeProfile());

        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("new@test.com");
        dto.setName("New Name");
        dto.setPassword("newPass");
        dto.setPhone("999");
        dto.setBirthDate(LocalDate.now());

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newPass")).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, EmployeeDTO.class)).thenReturn(dto);

        EmployeeDTO result = employeeService.updateEmployee(id, dto);

        assertEquals("new@test.com", user.getEmail());
        assertEquals("New Name", user.getName());
        assertEquals("encoded", user.getPassword());
        assertEquals("999", user.getEmployeeProfile().getPhone());
        assertNotNull(result);
    }

    @Test
    void updateEmployee_ShouldCreateProfile_IfNull() {
        Long id = 1L;
        User user = new User();
        user.setRoles(Set.of(Role.EMPLOYEE));
        user.setEmail("test@test.com");
        user.setEmployeeProfile(null);

        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("test@test.com");
        dto.setPhone("123");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, EmployeeDTO.class)).thenReturn(dto);

        employeeService.updateEmployee(id, dto);

        assertNotNull(user.getEmployeeProfile());
        assertEquals("123", user.getEmployeeProfile().getPhone());
    }

    @Test
    void updateEmployee_ShouldThrow_WhenEmailExists() {
        Long id = 1L;
        User user = new User();
        user.setRoles(Set.of(Role.EMPLOYEE));
        user.setEmail("old@test.com");

        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("exist@test.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(AlreadyExistException.class, () -> employeeService.updateEmployee(id, dto));
    }

    @Test
    void deleteEmployee_ShouldDelete() {
        Long id = 1L;
        when(userRepository.existsById(id)).thenReturn(true);

        employeeService.deleteEmployee(id);

        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteEmployee_ShouldThrow_WhenNotFound() {
        Long id = 1L;
        when(userRepository.existsById(id)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> employeeService.deleteEmployee(id));
    }

    @Test
    void addEmployee_ShouldSaveUser() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("new@test.com");
        dto.setPassword("pass");
        dto.setPhone("123");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(User.class), eq(EmployeeDTO.class))).thenReturn(dto);

        EmployeeDTO result = employeeService.addEmployee(dto);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addEmployee_ShouldThrow_WhenEmailExists() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("exist@test.com");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(AlreadyExistException.class, () -> employeeService.addEmployee(dto));
    }
}