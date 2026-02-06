package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Test
    @DisplayName("GET /login should return login view")
    void login_ShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET /client/register should return register view and model attribute")
    void register_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/client/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("client"));
    }
    @Test
    @DisplayName("POST /client/register success should redirect to login")
    void registerClient_Success() throws Exception {
        mockMvc.perform(post("/client/register")
                        .with(csrf())
                        .param("email", "new@test.com")
                        .param("password", "Password123")
                        .param("name", "John Doe")
                        .param("balance", "0.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(clientService, times(1)).addClient(any(ClientDTO.class));
    }


    @Test
    @DisplayName("POST /client/register with validation errors should return register view")
    void registerClient_ValidationErrors() throws Exception {
        mockMvc.perform(post("/client/register")
                        .with(csrf())
                        .param("email", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        verify(clientService, never()).addClient(any(ClientDTO.class));
    }



    @Test
    @DisplayName("POST /client/register when service throws exception (duplicate email) should return error")
    void registerClient_ServiceException() throws Exception {
        doThrow(new RuntimeException("Duplicate")).when(clientService).addClient(any(ClientDTO.class));

        mockMvc.perform(post("/client/register")
                        .with(csrf())
                        .param("email", "existing@test.com")
                        .param("password", "Password123")
                        .param("name", "Name")
                        .param("balance", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("client", "email"));

        verify(clientService).addClient(any(ClientDTO.class));
    }
}