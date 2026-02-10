package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.math.BigDecimal;
import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientProfileControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientProfileController clientProfileController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(clientProfileController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void getMyProfile_ShouldReturnProfileView() throws Exception {
        String email = "user@test.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        ClientDTO clientDTO = new ClientDTO();
        when(clientService.getClientByEmail(email)).thenReturn(clientDTO);

        mockMvc.perform(get("/profile").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("client", clientDTO));
    }

    @Test
    void showEditForm_ShouldReturnEditView() throws Exception {
        String email = "user@test.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        ClientDTO clientDTO = new ClientDTO();
        when(clientService.getClientByEmail(email)).thenReturn(clientDTO);

        mockMvc.perform(get("/profile/edit").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/edit"))
                .andExpect(model().attribute("client", clientDTO));
    }

    @Test
    void showAddBalanceForm_ShouldReturnTopupView() throws Exception {
        mockMvc.perform(get("/profile/topup"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/topup"));
    }

    @Test
    void topUpBalance_ShouldRedirectOnSuccess() throws Exception {
        String email = "user@test.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);
        BigDecimal amount = new BigDecimal("100.00");

        mockMvc.perform(post("/profile/topup")
                        .param("amount", "100.00")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?msg=profile.topup.success"));

        verify(clientService).topUpBalance(email, amount);
    }

    @Test
    void topUpBalance_ShouldRedirectWithError_WhenAmountNull() throws Exception {
        Principal principal = mock(Principal.class);

        mockMvc.perform(post("/profile/topup")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/topup?error=validation.required"));
    }

    @Test
    void topUpBalance_ShouldRedirectWithError_WhenAmountNegative() throws Exception {
        String email = "user@test.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);
        BigDecimal amount = new BigDecimal("-10.00");

        doThrow(new IllegalArgumentException("Negative")).when(clientService).topUpBalance(email, amount);

        mockMvc.perform(post("/profile/topup")
                        .param("amount", "-10.00")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/topup?error=validation.amount.negative"));
    }


    @Test
    void updateClient_ShouldReturnEditView_WhenValidationFails() throws Exception {
        mockMvc.perform(post("/profile/edit")
                        .param("name", "")
                        .param("email", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/edit"));

        verify(clientService, never()).updateClient(any(), any());
    }


}