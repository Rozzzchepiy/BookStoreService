package com.epam.rd.autocode.spring.project.component;

import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("balanceHelper")
@RequiredArgsConstructor
public class BalanceHelper {
    private final ClientService clientService;


    public BigDecimal getCurrentBalance() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return BigDecimal.ZERO;
        }

        try {
            String email = authentication.getName();
            return clientService.getClientByEmail(email).getBalance();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
