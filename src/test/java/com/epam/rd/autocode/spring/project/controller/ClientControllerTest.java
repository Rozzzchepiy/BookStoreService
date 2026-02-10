package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.criteria.ClientSearchRequest;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
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
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void getAllClients_ShouldReturnClientsView() throws Exception {
        Page<ClientDTO> page = new PageImpl<>(Collections.emptyList());
        when(clientService.getAllClients(any(ClientSearchRequest.class))).thenReturn(page);

        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(view().name("clients"))
                .andExpect(model().attributeExists("clients", "filter"));
    }

    @Test
    void getClientById_ShouldReturnClientView() throws Exception {
        Long id = 1L;
        ClientDTO clientDTO = new ClientDTO();
        when(clientService.getClientById(id)).thenReturn(clientDTO);

        mockMvc.perform(get("/clients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/client"))
                .andExpect(model().attribute("client", clientDTO));
    }

    @Test
    void deleteClientById_ShouldRedirectWithSuccessMessage() throws Exception {
        Long id = 1L;
        mockMvc.perform(post("/clients/delete/{id}", id)
                        .param("page", "0")
                        .param("keyword", "test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients?msg=client.delete.success&page=0&keyword=test"));

        verify(clientService).deleteClient(id);
    }

    @Test
    void deleteClientById_ShouldRedirectWithErrorMessage_WhenConstraintViolation() throws Exception {
        Long id = 1L;
        doThrow(new DataIntegrityViolationException("Constraint violation")).when(clientService).deleteClient(id);

        mockMvc.perform(post("/clients/delete/{id}", id)
                        .param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients?error=error.client.delete_constraint&page=0"));
    }

    @Test
    void updateClientStatus_ShouldBlockAndRedirect() throws Exception {
        Long id = 1L;
        mockMvc.perform(post("/clients/{id}/status", id)
                        .param("blocked", "true")
                        .param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients?page=0"));

        verify(clientService).blockClient(id);
    }

    @Test
    void updateClientStatus_ShouldUnblockAndRedirect() throws Exception {
        Long id = 1L;
        mockMvc.perform(post("/clients/{id}/status", id)
                        .param("blocked", "false")
                        .param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients?page=0"));

        verify(clientService).unblockClient(id);
    }

    @Test
    void blockClientFromDetails_ShouldBlockAndRedirect() throws Exception {
        Long id = 1L;
        mockMvc.perform(post("/clients/{id}/block", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/" + id + "?msg=client.block.success"));

        verify(clientService).blockClient(id);
    }

    @Test
    void blockClientFromDetails_ShouldHandleException() throws Exception {
        Long id = 1L;
        doThrow(new RuntimeException("Error")).when(clientService).blockClient(id);

        mockMvc.perform(post("/clients/{id}/block", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/" + id + "?error=common.error"));
    }

    @Test
    void unblockClientFromDetails_ShouldUnblockAndRedirect() throws Exception {
        Long id = 1L;
        mockMvc.perform(post("/clients/{id}/unblock", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/" + id + "?msg=client.unblock.success"));

        verify(clientService).unblockClient(id);
    }

    @Test
    void unblockClientFromDetails_ShouldHandleException() throws Exception {
        Long id = 1L;
        doThrow(new RuntimeException("Error")).when(clientService).unblockClient(id);

        mockMvc.perform(post("/clients/{id}/unblock", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/" + id + "?error=common.error"));
    }
}