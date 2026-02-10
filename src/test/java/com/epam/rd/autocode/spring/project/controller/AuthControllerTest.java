package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.RefreshToken;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.RefreshTokenService;
import com.epam.rd.autocode.spring.project.service.impl.JwtServiceImpl;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private ClientService clientService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtServiceImpl jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Виправляємо помилку "Circular view path"
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void loginPage_ShouldReturnLoginView_WhenNoRedirectUrl() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("redirectUrl"));
    }

    @Test
    void loginPage_ShouldReturnLoginView_WithRedirectUrl() throws Exception {
        String redirectUrl = "/books";
        mockMvc.perform(get("/login").param("redirect", redirectUrl))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("redirectUrl", redirectUrl));
    }

    @Test
    void registerPage_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/client/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("client"));
    }

    @Test
    void performLogin_ShouldLoginAndRedirectToBooks_WhenSuccess() throws Exception {
        String username = "user";
        String password = "password";
        UserDetails userDetails = new User(username, password, Collections.emptyList());
        Authentication authentication = mock(Authentication.class);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-value");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("access-token-value");
        when(refreshTokenService.createRefreshToken(username)).thenReturn(refreshToken);

        mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(cookie().value("accessToken", "access-token-value"))
                .andExpect(cookie().value("refreshToken", "refresh-token-value"));
    }

    @Test
    void performLogin_ShouldRedirectToProvidedUrl_WhenUrlIsValid() throws Exception {
        String username = "user";
        String password = "password";
        String redirectUrl = "/custom-path";
        UserDetails userDetails = new User(username, password, Collections.emptyList());
        Authentication authentication = mock(Authentication.class);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("rt");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("at");
        when(refreshTokenService.createRefreshToken(username)).thenReturn(refreshToken);

        mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", password)
                        .param("redirect", redirectUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redirectUrl));
    }

    @Test
    void performLogin_ShouldRedirectToBooks_WhenUrlIsUnsafe() throws Exception {
        String username = "user";
        String password = "password";
        String redirectUrl = "//google.com";
        UserDetails userDetails = new User(username, password, Collections.emptyList());
        Authentication authentication = mock(Authentication.class);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("rt");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("at");
        when(refreshTokenService.createRefreshToken(username)).thenReturn(refreshToken);

        mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", password)
                        .param("redirect", redirectUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    void performLogin_ShouldRedirectToBlocked_WhenDisabledException() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("User disabled"));

        mockMvc.perform(post("/login")
                        .param("username", "user")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=blocked"));
    }

    @Test
    void performLogin_ShouldRedirectToBlocked_WhenLockedException() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new LockedException("User locked"));

        mockMvc.perform(post("/login")
                        .param("username", "user")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=blocked"));
    }

    @Test
    void performLogin_ShouldRedirectToError_WhenAuthenticationException() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/login")
                        .param("username", "user")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void performLogout_ShouldClearContextAndCookies() throws Exception {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0))
                .andExpect(cookie().maxAge("JSESSIONID", 0));

        SecurityContextHolder.clearContext();
    }

    @Test
    void registerClient_ShouldRedirectToLogin_WhenSuccess() throws Exception {
        mockMvc.perform(post("/client/register")
                        .param("email", "new@test.com")
                        .param("password", "Password123")
                        .param("name", "Name Surname")
                        .param("balance", "100.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(clientService).addClient(any(ClientDTO.class));
    }

    @Test
    void registerClient_ShouldReturnRegister_WhenExceptionOccurs() throws Exception {
        doThrow(new RuntimeException("Email exists")).when(clientService).addClient(any());

        mockMvc.perform(post("/client/register")
                        .param("email", "exist@test.com")
                        .param("password", "Password123")
                        .param("name", "Name Surname")
                        .param("balance", "100.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void registerClient_ShouldReturnRegister_WhenValidationFails() throws Exception {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        AuthController validationController = new AuthController(clientService, authenticationManager, jwtService, refreshTokenService) {
            @Override
            public String registerClient(ClientDTO clientDTO, BindingResult result) {
                result.rejectValue("email", "error");
                return super.registerClient(clientDTO, result);
            }
        };
        MockMvc validationMockMvc = MockMvcBuilders.standaloneSetup(validationController)
                .setViewResolvers(viewResolver)
                .build();

        mockMvc.perform(post("/client/register")
                        .param("email", "")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        verify(clientService, never()).addClient(any());
    }
}