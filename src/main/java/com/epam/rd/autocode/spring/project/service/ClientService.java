package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.criteria.ClientSearchRequest;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ClientService {

    void increaseFailedAttempts(User user);
    void resetFailedAttempts(String email);
    void lock(User user);
    boolean unlockWhenTimeExpired(User user);
    void blockClient(Long id);
    void unblockClient(Long id);
    Page<ClientDTO> getAllClients(ClientSearchRequest request);

    ClientDTO getClientById(Long id);

    ClientDTO getClientByEmail(String email);

    ClientDTO updateClient(Long id, ClientDTO client);

    void deleteClient(Long id);

    ClientDTO addClient(ClientDTO client);
    void topUpBalance(String email, BigDecimal amount);
}
