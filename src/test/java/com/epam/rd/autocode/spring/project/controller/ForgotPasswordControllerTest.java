package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.PasswordResetDTO;
import com.epam.rd.autocode.spring.project.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordControllerTest {

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private ForgotPasswordController forgotPasswordController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(forgotPasswordController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void showForgotPasswordForm_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot_password"));
    }

    @Test
    void processForgotPassword_ShouldInitiateResetAndRedirect() throws Exception {
        String email = "test@test.com";

        mockMvc.perform(post("/forgot-password")
                        .param("email", email))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/forgot-password?status=sent"));

        verify(passwordResetService).initiatePasswordReset(eq(email), contains("http://localhost"));
    }

    @Test
    void showResetPasswordForm_ShouldReturnResetView_WhenTokenValid() throws Exception {
        String token = "valid-token";
        when(passwordResetService.validatePasswordResetToken(token)).thenReturn(true);

        mockMvc.perform(get("/forgot-password/reset")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name("reset_password"))
                .andExpect(model().attributeExists("resetForm"));
    }

    @Test
    void showResetPasswordForm_ShouldReturnErrorView_WhenTokenInvalid() throws Exception {
        String token = "invalid-token";
        when(passwordResetService.validatePasswordResetToken(token)).thenReturn(false);

        mockMvc.perform(get("/forgot-password/reset")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot_password"))
                .andExpect(model().attribute("error", "invalid_token"));
    }

    @Test
    void processResetPassword_ShouldUpdatePasswordAndRedirect() throws Exception {
        mockMvc.perform(post("/forgot-password/reset")
                        .param("token", "valid-token")
                        .param("password", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?msg=password_changed"));

        verify(passwordResetService).updatePassword("valid-token", "newPassword123");
    }

    @Test
    void processResetPassword_ShouldReturnForm_WhenValidationFails() throws Exception {
        mockMvc.perform(post("/forgot-password/reset")
                        .param("token", "valid-token")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("reset_password"));

        verify(passwordResetService, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void processResetPassword_ShouldReturnForm_WhenServiceThrowsException() throws Exception {
        doThrow(new RuntimeException("Error")).when(passwordResetService).updatePassword(anyString(), anyString());

        mockMvc.perform(post("/forgot-password/reset")
                        .param("token", "valid-token")
                        .param("password", "newPassword123"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset_password"))
                .andExpect(model().attribute("error", "update_failed"));
    }
}