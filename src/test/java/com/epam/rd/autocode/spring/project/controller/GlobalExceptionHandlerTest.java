package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.security.Principal;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/not-found-exception")
        public void throwNotFoundException() {
            throw new NotFoundException("Not Found");
        }

        @GetMapping("/test/access-denied")
        public void throwAccessDeniedException() {
            throw new AccessDeniedException("Access Denied");
        }

        @GetMapping("/test/runtime-exception")
        public void throwRuntimeException() {
            throw new RuntimeException("Runtime Error");
        }

        @GetMapping("/test/exception")
        public void throwException() throws Exception {
            throw new Exception("General Error");
        }
    }

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(globalExceptionHandler)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void handleNotFoundException_ShouldReturnErrorView() throws Exception {
        when(messageSource.getMessage(eq("error.404.resource"), any(), any(Locale.class)))
                .thenReturn("Resource Not Found Message");

        mockMvc.perform(get("/test/not-found-exception"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("status", 404))
                .andExpect(model().attribute("error", "Resource Not Found Message"));
    }

    @Test
    void handleAccessDeniedException_ShouldReturnErrorView() throws Exception {
        when(messageSource.getMessage(eq("error.403"), any(), any(Locale.class)))
                .thenReturn("Access Denied Message");

        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("status", 403))
                .andExpect(model().attribute("error", "Access Denied Message"));
    }

    @Test
    void handleRuntimeException_ShouldReturnErrorView() throws Exception {
        when(messageSource.getMessage(eq("error.400"), any(), any(Locale.class)))
                .thenReturn("Bad Request Message");

        mockMvc.perform(get("/test/runtime-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("status", 400))
                .andExpect(model().attribute("error", "Bad Request Message"));
    }

    @Test
    void handleGlobalException_ShouldReturnErrorView() throws Exception {
        when(messageSource.getMessage(eq("error.500"), any(), any(Locale.class)))
                .thenReturn("Internal Server Error Message");

        mockMvc.perform(get("/test/exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("status", 500))
                .andExpect(model().attribute("error", "Internal Server Error Message"));
    }

    @Test
    void handleNoHandlerFoundException_ShouldReturnErrorView() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/unknown", null);
        org.springframework.ui.Model model = mock(org.springframework.ui.Model.class);
        Principal principal = mock(Principal.class);
        Locale locale = Locale.ENGLISH;

        when(principal.getName()).thenReturn("User");
        when(messageSource.getMessage("error.404.page", null, locale)).thenReturn("Page Not Found");

        String viewName = globalExceptionHandler.handleNoHandlerFoundException(ex, model, principal, locale);

        org.junit.jupiter.api.Assertions.assertEquals("error", viewName);
        org.mockito.Mockito.verify(model).addAttribute("status", 404);
        org.mockito.Mockito.verify(model).addAttribute("error", "Page Not Found");
    }
}