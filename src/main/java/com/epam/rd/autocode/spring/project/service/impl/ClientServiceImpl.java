package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.annotation.Loggable;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.ClientProfile;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${security.login.lock-duration-minutes}")
    private long lockTimeDuration;

    @Value("${security.login.max-attempts}")
    private int maxFailedAttempts;

    @Transactional
    @Override
    public void increaseFailedAttempts(User user) {
        int newFailAttempts = user.getFailedAttempt() + 1;
        user.setFailedAttempt(newFailAttempts);

        if (user.getFailedAttempt() >= maxFailedAttempts) {
            lock(user);
        }
        userRepository.save(user);
    }

    @Transactional
    @Loggable
    public void resetFailedAttempts(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepository.save(user);
        });
    }

    @Transactional
    @Override
    @Loggable
    public void lock(User user) {
        user.setLockTime(LocalDateTime.now().plusMinutes(lockTimeDuration));
    }

    @Transactional
    @Override
    @Loggable
    public boolean unlockWhenTimeExpired(User user) {
        if (user.getLockTime() != null) {
            if (user.getLockTime().isBefore(LocalDateTime.now())) {
                user.setLockTime(null);
                user.setFailedAttempt(0);
                userRepository.save(user);
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    @Loggable
    @Transactional
    public void blockClient(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setBlocked(true);
        userRepository.save(user);
    }

    @Override
    @Loggable
    @Transactional
    public void unblockClient(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setBlocked(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientDTO> getAllClients(Pageable pageable, String keyword) {
        Page<User> clientsPage;

        if (keyword != null && !keyword.isBlank()) {
            clientsPage = userRepository.findAllByRolesContainingAndKeyword(Role.CLIENT, keyword, pageable);
        } else {
            clientsPage = userRepository.findAllByRolesContaining(Role.CLIENT, pageable);
        }

        return clientsPage.map(this::mapper);
    }

    @Override
    public ClientDTO getClientById(Long id) {
        User client = userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Client not found"));

        if(!client.getRoles().contains(Role.CLIENT)){
            throw new NotFoundException("This user is not a client");
        }

        return mapper(client);
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        User client = userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("Client not found"));

        return mapper(client);
    }

    @Override
    @Loggable
    @Transactional
    public ClientDTO updateClient(Long id, ClientDTO client) {
        User user = userRepository.findById(id).orElseThrow(()-> new NotFoundException("Client not found"));
        if(!user.getRoles().contains(Role.CLIENT)){
            throw new NotFoundException("This user is not a client");
        }
        if (!user.getEmail().equals(client.getEmail())) {
            if (userRepository.findByEmail(client.getEmail()).isPresent()) {
                throw new AlreadyExistException("Client with this email already exists");
            }
            user.setEmail(client.getEmail());
        }
        user.setName(client.getName());

        if (client.getPassword() != null && !client.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(client.getPassword()));
        }

        user.getClientProfile().setBalance(client.getBalance());

        User updatedUser = userRepository.save(user);
        return mapper(updatedUser);
    }

    @Override
    @Loggable
    @Transactional
    public void deleteClient(Long id) {
        User user =  userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Client not found"));
        userRepository.delete(user);
    }

    @Override
    @Loggable
    @Transactional
    public ClientDTO addClient(ClientDTO client) {
        if (userRepository.findByEmail(client.getEmail()).isPresent()) {
            throw new AlreadyExistException("Client already exists");
        }
        User newUser = new User();
        newUser.setName(client.getName());
        newUser.setEmail(client.getEmail());
        String encodedPassword = passwordEncoder.encode(client.getPassword());

        newUser.setPassword(encodedPassword);
        newUser.setRoles(new HashSet<>(Set.of(Role.CLIENT)));

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setBalance(client.getBalance());

        clientProfile.setUser(newUser);
        newUser.setClientProfile(clientProfile);

        User savedUser = userRepository.save(newUser);

        return mapper(savedUser);
    }

    @Override
    @Loggable
    @Transactional
    public void topUpBalance(String email, BigDecimal amount) {
        User user =  userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("User not found"));
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сума поповнення має бути додатною");
        }
        user.getClientProfile().setBalance(user.getClientProfile().getBalance().add(amount));
        userRepository.save(user);
    }

    private ClientDTO mapper(User user) {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(user.getId());
        clientDTO.setEmail(user.getEmail());
        clientDTO.setName(user.getName());
        clientDTO.setPassword(null);
        clientDTO.setBalance(user.getClientProfile().getBalance());
        clientDTO.setBlocked(user.isBlocked());
        return clientDTO;
    }
}
