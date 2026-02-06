package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    private ClientDTO clientDTO;
    private final String TEST_EMAIL = "client@test.com";

    @BeforeEach
    void setUp() {
        clientDTO = new ClientDTO();
        clientDTO.setId(1L);
        clientDTO.setEmail(TEST_EMAIL);
        clientDTO.setName("Test Client");
        clientDTO.setBalance(new BigDecimal("100.00"));
        clientDTO.setPassword("Password123");
    }


    @Test
    @DisplayName("GET /profile should return profile view with client data")
    void getMyProfile_ShouldReturnProfileView() throws Exception {
        when(clientService.getClientByEmail(TEST_EMAIL)).thenReturn(clientDTO);

        mockMvc.perform(get("/profile")
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("client", clientDTO));

        verify(clientService).getClientByEmail(TEST_EMAIL);
    }


    @Test
    @DisplayName("GET /profile/edit should return edit view")
    void showEditForm_ShouldReturnEditView() throws Exception {
        when(clientService.getClientByEmail(TEST_EMAIL)).thenReturn(clientDTO);

        mockMvc.perform(get("/profile/edit")
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/edit"))
                .andExpect(model().attribute("client", clientDTO));
    }


    @Test
    @DisplayName("POST /profile/edit success should redirect")
    void updateClient_Success() throws Exception {
        when(clientService.getClientByEmail(TEST_EMAIL)).thenReturn(clientDTO);

        mockMvc.perform(post("/profile/edit")
                        .with(csrf())
                        .principal(() -> TEST_EMAIL)
                        .param("name", "Updated Name")
                        .param("email", "new@test.com")
                        .param("password", "Password123")
                        .param("balance", "100.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(clientService).updateClient(eq(1L), any(ClientDTO.class));
    }

    @Test
    @DisplayName("POST /profile/edit validation error should return edit view")
    void updateClient_ValidationError() throws Exception {
        mockMvc.perform(post("/profile/edit")
                        .with(csrf())
                        .principal(() -> TEST_EMAIL)
                        .param("email", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/edit"))
                .andExpect(model().hasErrors());

        verify(clientService, never()).updateClient(any(), any());
    }

    @Test
    @DisplayName("POST /profile/edit exception (e.g. duplicate email) should return edit view with error")
    void updateClient_ServiceException() throws Exception {
        when(clientService.getClientByEmail(TEST_EMAIL)).thenReturn(clientDTO);

        doThrow(new RuntimeException("Duplicate")).when(clientService).updateClient(any(), any());

        mockMvc.perform(post("/profile/edit")
                        .with(csrf())
                        .principal(() -> TEST_EMAIL)
                        .param("name", "Updated Name")
                        .param("email", "busy@test.com")
                        .param("password", "Password123")
                        .param("balance", "100.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/edit"))
                .andExpect(model().attributeHasFieldErrors("client", "email"));     }


    @Test
    @DisplayName("GET /profile/topup should return topup view")
    void showAddBalanceForm_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/profile/topup"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/topup"));
    }


    @Test
    @DisplayName("POST /profile/topup success should redirect to books")
    void topUpBalance_Success() throws Exception {
        mockMvc.perform(post("/profile/topup")
                        .with(csrf())
                        .principal(() -> TEST_EMAIL)
                        .param("amount", "50.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(clientService).topUpBalance(TEST_EMAIL, new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("POST /profile/topup with negative amount should return topup view with error")
    void topUpBalance_NegativeAmount() throws Exception {
        // Імітуємо викидання виключення сервісом
        doThrow(new IllegalArgumentException("Negative amount"))
                .when(clientService).topUpBalance(eq(TEST_EMAIL), any(BigDecimal.class));

        mockMvc.perform(post("/profile/topup")
                        .with(csrf())
                        .principal(() -> TEST_EMAIL)
                        .param("amount", "-10.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/topup"))
                .andExpect(model().attributeExists("error"));
    }
}