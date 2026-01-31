package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.ClientProfile;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> getAllClients() {
        List<User> clients = userRepository.findAllByRolesContaining(Role.CLIENT);
        return clients.stream()
                .map(this::mapper)
                .collect(Collectors.toList());
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
    @Transactional
    public void deleteClient(Long id) {
        User user =  userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Client not found"));
        userRepository.delete(user);
    }

    @Override
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

    private ClientDTO mapper(User user) {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(user.getId());
        clientDTO.setEmail(user.getEmail());
        clientDTO.setName(user.getName());
        clientDTO.setPassword(null);
        clientDTO.setBalance(user.getClientProfile().getBalance());
        return clientDTO;
    }
}
