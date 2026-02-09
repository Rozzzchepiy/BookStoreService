package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class UserBlockingFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public UserBlockingFilter(@Lazy UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {

            String email = auth.getName();

            log.debug("BLOCKING_FILTER: Checking status for user: {}", email);

            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                boolean isLockedByTime = user.getLockTime() != null && user.getLockTime().isAfter(java.time.LocalDateTime.now());

                if (user.isBlocked() || isLockedByTime) {

                    log.warn("SECURITY ALERT: Blocked user '{}' tried to access system. Reason: Blocked={}, LockedByTime={}. Terminating session.",
                            email, user.isBlocked(), isLockedByTime);

                    SecurityContextHolder.clearContext();

                    removeCookie(response, "accessToken");
                    removeCookie(response, "refreshToken");

                    response.sendRedirect("/login?error=blocked");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void removeCookie(HttpServletResponse response, String name) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}