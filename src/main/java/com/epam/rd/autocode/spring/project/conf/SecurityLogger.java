package com.epam.rd.autocode.spring.project.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityLogger {

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.info("LOGIN SUCCESS: User '{}' увійшов у систему", username);
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();
        String error = event.getException().getMessage();
        log.warn("LOGIN FAILED: Спроба входу для '{}'. Причина: {}", username, error);
    }

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        String username = (event.getAuthentication() != null) ? event.getAuthentication().getName() : "Unknown";
        log.info("LOGOUT: User '{}' вийшов із системи", username);
    }
}