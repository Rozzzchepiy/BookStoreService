package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.criteria.EmployeeSearchRequest;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void getAllEmployees_ShouldReturnEmployeesView() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(Collections.emptyList());
        when(employeeService.getAllEmployees(any(EmployeeSearchRequest.class))).thenReturn(page);

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees"))
                .andExpect(model().attributeExists("employees", "filter"));
    }

    @Test
    void createEmployeeForm_ShouldReturnFormView() throws Exception {
        mockMvc.perform(get("/employees/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_form"))
                .andExpect(model().attributeExists("employee"));
    }



    @Test
    void createEmployee_ShouldReturnForm_WhenValidationFails() throws Exception {
        mockMvc.perform(post("/employees/add")
                        .param("email", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_form"));

        verify(employeeService, never()).addEmployee(any());
    }



    @Test
    void getEmployeeById_ShouldReturnEmployeeView() throws Exception {
        Long id = 1L;
        EmployeeDTO dto = new EmployeeDTO();
        when(employeeService.getEmployeeById(id)).thenReturn(dto);

        mockMvc.perform(get("/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("employee"))
                .andExpect(model().attribute("employee", dto));
    }

    @Test
    void editEmployeeForm_ShouldReturnFormView() throws Exception {
        Long id = 1L;
        EmployeeDTO dto = new EmployeeDTO();
        when(employeeService.getEmployeeById(id)).thenReturn(dto);

        mockMvc.perform(get("/employees/edit/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_form"))
                .andExpect(model().attribute("employee", dto));
    }



    @Test
    void updateEmployee_ShouldReturnForm_WhenValidationFails() throws Exception {
        Long id = 1L;
        mockMvc.perform(post("/employees/edit/{id}", id)
                        .param("email", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_form"));

        verify(employeeService, never()).updateEmployee(anyLong(), any());
    }



    @Test
    void deleteEmployee_ShouldRedirectOnSuccess() throws Exception {
        Long id = 1L;
        mockMvc.perform(post("/employees/delete/{id}", id)
                        .param("page", "1")
                        .param("keyword", "search"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees?msg=employee.delete.success&page=1&keyword=search"));

        verify(employeeService).deleteEmployee(id);
    }

    @Test
    void deleteEmployee_ShouldRedirectWithError_WhenIntegrityViolation() throws Exception {
        Long id = 1L;
        doThrow(new DataIntegrityViolationException("Constraint")).when(employeeService).deleteEmployee(id);

        mockMvc.perform(post("/employees/delete/{id}", id)
                        .param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees?error=error.employee.delete_constraint&page=0"));
    }
}