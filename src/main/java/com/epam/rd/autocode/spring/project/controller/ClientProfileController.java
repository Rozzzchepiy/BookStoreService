package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ClientProfileController {
    private final ClientService clientService;

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN', 'CLIENT')")
    @PostMapping("/edit")
    public String updateClient(@Valid @ModelAttribute("client") ClientDTO clientDTO, BindingResult bindingResult,
                               Principal principal){

        if (bindingResult.hasErrors()){
            return "profile/edit";
        }
        String email = principal.getName();
        ClientDTO client = clientService.getClientByEmail(email);
        clientService.updateClient(client.getId(), clientDTO);

        return "redirect:/profile";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN', 'CLIENT')")
    @GetMapping("/edit")
    public String showEditForm(Model model, Principal principal){
        String email = principal.getName();
        ClientDTO client = clientService.getClientByEmail(email);
        client.setPassword(null);
        model.addAttribute("client", client );
        return "profile/edit";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN', 'CLIENT')")
    @GetMapping()
    public String getMyProfile(Model model, Principal principal){
        String email = principal.getName();
        ClientDTO client = clientService.getClientByEmail(email);
        client.setPassword(null);
        model.addAttribute("client", client);
        return "profile";
    }
}
