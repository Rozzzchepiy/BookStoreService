package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(1L);
        employeeDTO.setEmail("employee@test.com");
        employeeDTO.setName("John Doe");
        employeeDTO.setPhone("0987654321");
        employeeDTO.setBirthDate(LocalDate.now().minusYears(25));
        employeeDTO.setPassword("Password123");
    }

    @Test
    @DisplayName("GET /employees should return list view")
    void getAllEmployees_ShouldReturnListView() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(employeeDTO));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees"))
                .andExpect(model().attributeExists("employees"));

        verify(employeeService).getAllEmployees();
    }

    @Test
    @DisplayName("GET /employees/{id} should return employee details view")
    void getEmployeeById_ShouldReturnDetailsView() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employeeDTO);

        mockMvc.perform(get("/employees/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("employee"))
                .andExpect(model().attribute("employee", employeeDTO));

        verify(employeeService).getEmployeeById(1L);
    }

    @Test
    @DisplayName("GET /employees/add should return create form")
    void createEmployeeForm_ShouldReturnFormView() throws Exception {
        mockMvc.perform(get("/employees/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_form"))
                .andExpect(model().attributeExists("employee"));
    }

    @Test
    @DisplayName("POST /employees/add success should redirect")
    void createEmployee_Success() throws Exception {
        mockMvc.perform(post("/employees/add")
                        .with(csrf())
                        .param("email", "new@test.com")
                        .param("name", "New Employee")
                        .param("password", "Password123")
                        .param("phone", "0123456789")
                        .param("birthDate", LocalDate.now().minusYears(20).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService).addEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("POST /employees/add validation error should return form")
    void createEmployee_ValidationError() throws Exception {
        mockMvc.perform(post("/employees/add")
                        .with(csrf())
                        .param("email", "")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_form"))
                .andExpect(model().hasErrors());

        verify(employeeService, never()).addEmployee(any());
    }

    @Test
    @DisplayName("POST /employees/add service exception should return form with error")
    void createEmployee_ServiceException() throws Exception {
        doThrow(new RuntimeException("Duplicate")).when(employeeService).addEmployee(any(EmployeeDTO.class));

        mockMvc.perform(post("/employees/add")
                        .with(csrf())
                        .param("email", "existing@test.com")
                        .param("name", "Name")
                        .param("password", "Password123")
                        .param("phone", "0123456789")
                        .param("birthDate", LocalDate.now().minusYears(20).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_form"))
                .andExpect(model().attributeHasFieldErrors("employee", "email"));

        verify(employeeService).addEmployee(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("GET /employees/edit/{id} should return edit form")
    void editEmployeeForm_ShouldReturnFormView() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employeeDTO);

        mockMvc.perform(get("/employees/edit/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_form"))
                .andExpect(model().attribute("employee", employeeDTO));
    }

    @Test
    @DisplayName("POST /employees/edit/{id} success should redirect")
    void updateEmployee_Success() throws Exception {
        mockMvc.perform(post("/employees/edit/{id}", 1L)
                        .with(csrf())
                        .param("id", "1")
                        .param("email", "updated@test.com")
                        .param("name", "Updated Name")
                        .param("password", "Password123")
                        .param("phone", "0987654321")
                        .param("birthDate", LocalDate.now().minusYears(25).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService).updateEmployee(eq(1L), any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("POST /employees/edit/{id} validation error should return form")
    void updateEmployee_ValidationError() throws Exception {
        mockMvc.perform(post("/employees/edit/{id}", 1L)
                        .with(csrf())
                        .param("email", "invalid-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_form"))
                .andExpect(model().hasErrors());

        verify(employeeService, never()).updateEmployee(anyLong(), any());
    }

    @Test
    @DisplayName("POST /employees/edit/{id} service exception should return form with error")
    void updateEmployee_ServiceException() throws Exception {
        doThrow(new RuntimeException("Duplicate")).when(employeeService).updateEmployee(eq(1L), any(EmployeeDTO.class));

        mockMvc.perform(post("/employees/edit/{id}", 1L)
                        .with(csrf())
                        .param("email", "busy@test.com")
                        .param("name", "Name")
                        .param("password", "Password123")
                        .param("phone", "0987654321")
                        .param("birthDate", LocalDate.now().minusYears(25).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_form"))
                .andExpect(model().attributeHasFieldErrors("employee", "email"));

        verify(employeeService).updateEmployee(eq(1L), any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("POST /employees/delete/{id} success should redirect")
    void deleteEmployee_Success() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(post("/employees/delete/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeCount(0));

        verify(employeeService).deleteEmployee(1L);
    }

    @Test
    @DisplayName("POST /employees/delete/{id} constraint violation should redirect with flash error")
    void deleteEmployee_ConstraintViolation() throws Exception {
        doThrow(new DataIntegrityViolationException("Constraint")).when(employeeService).deleteEmployee(1L);

        mockMvc.perform(post("/employees/delete/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("error", "error.employee.delete_constraint"));

        verify(employeeService).deleteEmployee(1L);
    }
}