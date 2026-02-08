package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
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
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent() && userOptional.get().isBlocked()) {

                SecurityContextHolder.clearContext();

                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }

                response.sendRedirect("/login?error");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}