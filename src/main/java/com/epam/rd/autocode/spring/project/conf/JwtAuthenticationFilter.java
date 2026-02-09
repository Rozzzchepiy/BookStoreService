package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.service.impl.JwtService;
import com.epam.rd.autocode.spring.project.service.impl.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = getCookieValue(request, "accessToken");
        String refreshToken = getCookieValue(request, "refreshToken");

        if (jwt != null) {
            try {
                String userEmail = jwtService.extractUsername(jwt);

                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    log.debug("JWT: Access Token found for user: {}", userEmail);

                    try {
                        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                        if (jwtService.isTokenValid(jwt, userDetails)) {
                            authenticateUser(userDetails, request);
                            log.debug("JWT: User '{}' authenticated via Access Token", userEmail);
                        }
                    } catch (UsernameNotFoundException e) {
                        log.warn("JWT: User not found for token: {}", userEmail);
                    }
                }
            } catch (Exception e) {
                log.debug("JWT: Access Token invalid or expired. Reason: {}", e.getMessage());
            }
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null && refreshToken != null) {
            log.debug("JWT: Attempting Silent Refresh...");
            try {
                refreshTokenService.findByToken(refreshToken)
                        .map(refreshTokenService::verifyExpiration)
                        .ifPresent(token -> {
                            try {
                                UserDetails userDetails = userDetailsService.loadUserByUsername(token.getUser().getEmail());
                                String newAccessToken = jwtService.generateToken(userDetails);

                                addNewAccessTokenCookie(response, newAccessToken);
                                authenticateUser(userDetails, request);

                                log.info("SECURITY: Silent Refresh SUCCESS for user: {}", userDetails.getUsername());

                            } catch (UsernameNotFoundException ex) {
                                log.warn("SECURITY: Refresh failed - User not found. Deleting token.");
                                refreshTokenService.deleteByUserId(token.getUser().getId());
                            }
                        });
            } catch (Exception ex) {
                log.warn("SECURITY: Silent Refresh FAILED. Reason: {}", ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private String getCookieValue(HttpServletRequest req, String cookieName) {
        if (req.getCookies() != null) {
            return Arrays.stream(req.getCookies())
                    .filter(c -> c.getName().equals(cookieName))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }

    private void addNewAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(15 * 60);
        response.addCookie(cookie);
    }
}