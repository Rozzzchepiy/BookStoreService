package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.annotation.Loggable;
import com.epam.rd.autocode.spring.project.criteria.ClientSearchRequest;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.ClientProfile;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

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
        changeBlockStatus(id, true);
    }

    @Override
    @Loggable
    @Transactional
    public void unblockClient(Long id) {
        changeBlockStatus(id, false);
    }

    private void changeBlockStatus(Long id, boolean status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setBlocked(status);
        userRepository.save(user);
    }

    @Override
    public Page<ClientDTO> getAllClients(ClientSearchRequest request) {
        Page<User> clientsPage;

        if (StringUtils.hasText(request.getKeyword())) {
            clientsPage = userRepository.findAllByRolesContainingAndKeyword(
                    Role.CLIENT, request.getKeyword(), request.getPageable());
        } else {
            clientsPage = userRepository.findAllByRolesContaining(
                    Role.CLIENT, request.getPageable());
        }

        return clientsPage.map(this::mapToDto);
    }

    @Override
    public ClientDTO getClientById(Long id) {
        User user = getUserByIdAndRole(id, Role.CLIENT);
        ClientDTO dto = mapToDto(user);
        dto.setPassword(null);
        return dto;
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        User client = userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("Client not found"));
        return mapToDto(client);
    }


    private User getUserByIdAndRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!user.getRoles().contains(role)) {
            throw new NotFoundException("User is not a " + role);
        }
        return user;
    }


    @Override
    @Loggable
    @Transactional
    public ClientDTO updateClient(Long id, ClientDTO clientDTO) {
        User user = getUserByIdAndRole(id, Role.CLIENT);

        if (!user.getEmail().equals(clientDTO.getEmail())) {
            if (userRepository.findByEmail(clientDTO.getEmail()).isPresent()) {
                throw new AlreadyExistException("Client with this email already exists");
            }
            user.setEmail(clientDTO.getEmail());
        }

        user.setName(clientDTO.getName());

        if (StringUtils.hasText(clientDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        }

        if (clientDTO.getBalance() != null) {
            user.getClientProfile().setBalance(clientDTO.getBalance());
        }

        return mapToDto(userRepository.save(user));
    }

    @Override
    @Loggable
    @Transactional
    public void deleteClient(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Client not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Loggable
    @Transactional
    public ClientDTO addClient(ClientDTO clientDTO) {
        if (userRepository.findByEmail(clientDTO.getEmail()).isPresent()) {
            throw new AlreadyExistException("Client already exists");
        }

        User newUser = new User();
        newUser.setName(clientDTO.getName());
        newUser.setEmail(clientDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        newUser.setRoles(Set.of(Role.CLIENT));

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setBalance(clientDTO.getBalance() != null ? clientDTO.getBalance() : BigDecimal.ZERO);

        newUser.setClientProfile(clientProfile);
        clientProfile.setUser(newUser);

        return mapToDto(userRepository.save(newUser));
    }

    @Override
    @Loggable
    @Transactional
    public void topUpBalance(String email, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ClientProfile profile = user.getClientProfile();
        profile.setBalance(profile.getBalance().add(amount));

        userRepository.save(user);
    }

    private ClientDTO mapToDto(User user) {
        ClientDTO dto = modelMapper.map(user, ClientDTO.class);

        if (user.getClientProfile() != null) {
            dto.setBalance(user.getClientProfile().getBalance());
        }

        return dto;
    }

}
