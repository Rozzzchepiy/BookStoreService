package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.RefreshToken;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.RefreshTokenService;
import com.epam.rd.autocode.spring.project.service.impl.JwtServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final ClientService clientService;

    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtService;
    private final RefreshTokenService refreshTokenService;


    @GetMapping("/login")
    public String loginPage(
            Model model,
            @RequestParam(name = "redirect", required = false) String redirectUrl
    ) {
        if (redirectUrl != null) {
            model.addAttribute("redirectUrl", redirectUrl);
        }
        return "login";
    }

    @GetMapping("/client/register")
    public String registerPage(Model model) {
        model.addAttribute("client", new ClientDTO());
        return "register";
    }


    @PostMapping("/login")
    public String performLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(name = "redirect", required = false) String redirectUrl,
            HttpServletResponse response
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String accessToken = jwtService.generateToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

            addCookie(response, "accessToken", accessToken, 15 * 60);
            addCookie(response, "refreshToken", refreshToken.getToken(), 7 * 24 * 60 * 60);

            if (redirectUrl != null && !redirectUrl.isBlank()) {
                if (redirectUrl.startsWith("/") && !redirectUrl.startsWith("//")) {
                    return "redirect:" + redirectUrl;
                }
            }

            return "redirect:/books";
        } catch (DisabledException |    LockedException e) {
            return "redirect:/login?error=blocked";

        } catch (AuthenticationException e) {
            return "redirect:/login?error";
        }
    }


    @PostMapping("/logout")
    public String performLogout(HttpServletResponse response) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("AUDIT: Logout initiated for user: {}", username);
        deleteCookie(response, "accessToken");
        deleteCookie(response, "refreshToken");
        deleteCookie(response, "JSESSIONID");

        SecurityContextHolder.clearContext();
        return "redirect:/login?logout";
    }

    @PostMapping("/client/register")
    public String registerClient(@Valid @ModelAttribute("client") ClientDTO clientDTO, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            clientService.addClient(clientDTO);
        } catch (Exception ex) {
            result.rejectValue("email", "validation.email.exists", "User with this email already exists");
            return "register";
        }
        return "redirect:/login";
    }


    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}