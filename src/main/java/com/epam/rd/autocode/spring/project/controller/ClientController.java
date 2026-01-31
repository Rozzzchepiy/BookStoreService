package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController { 

    private final ClientService clientService;

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping
    public String getAllClients(Model model){
        List<ClientDTO> clients = clientService.getAllClients();
        clients.forEach(e -> e.setPassword(null));
        model.addAttribute("clients", clients);
        return "clients";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/{id}")
    public String getClientById(@PathVariable("id") Long id, Model model){
        ClientDTO clientDTO = clientService.getClientById(id);
        clientDTO.setPassword(null);
        model.addAttribute("client", clientDTO);
        return "clients/client";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteClientById(@PathVariable("id") Long id){
        clientService.deleteClient(id);
        return "redirect:/clients";
    }


}
