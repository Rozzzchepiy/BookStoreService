package com.epam.rd.autocode.spring.project.component;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
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

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return BigDecimal.ZERO;
        }

        try {
            String email = authentication.getName();
            ClientDTO client = clientService.getClientByEmail(email);

            if (client != null && client.getBalance() != null) {
                return client.getBalance();
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.ZERO;
    }
}