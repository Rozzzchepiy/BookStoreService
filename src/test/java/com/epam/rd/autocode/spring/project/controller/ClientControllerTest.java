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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    private ClientDTO clientDTO;

    @BeforeEach
    void setUp() {
        clientDTO = new ClientDTO();
        clientDTO.setId(1L);
        clientDTO.setEmail("client@test.com");
        clientDTO.setName("Test Client");
        clientDTO.setBalance(new BigDecimal("100.00"));
        clientDTO.setPassword("Password123");
    }


    @Test
    @DisplayName("GET /clients should return list view")
    void getAllClients_ShouldReturnListView() throws Exception {
        when(clientService.getAllClients()).thenReturn(List.of(clientDTO));

        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(view().name("clients"))
                .andExpect(model().attributeExists("clients"));

        verify(clientService).getAllClients();
    }


    @Test
    @DisplayName("GET /clients/{id} should return client details view")
    void getClientById_ShouldReturnDetailsView() throws Exception {
        when(clientService.getClientById(1L)).thenReturn(clientDTO);

        mockMvc.perform(get("/clients/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/client"))
                .andExpect(model().attribute("client", clientDTO));

        verify(clientService).getClientById(1L);
    }


    @Test
    @DisplayName("POST /clients/delete/{id} success should redirect")
    void deleteClientById_Success() throws Exception {
        doNothing().when(clientService).deleteClient(1L);

        mockMvc.perform(post("/clients/delete/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"))
                .andExpect(flash().attributeCount(0));

        verify(clientService).deleteClient(1L);
    }

    @Test
    @DisplayName("POST /clients/delete/{id} with constraint violation should redirect with flash error")
    void deleteClientById_ConstraintViolation() throws Exception {
        doThrow(new DataIntegrityViolationException("Constraint violation"))
                .when(clientService).deleteClient(1L);

        mockMvc.perform(post("/clients/delete/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"))
                .andExpect(flash().attribute("error", "error.client.delete_constraint"));

        verify(clientService).deleteClient(1L);
    }
}