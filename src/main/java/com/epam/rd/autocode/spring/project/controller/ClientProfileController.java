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

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ClientProfileController {
    private final ClientService clientService;

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/edit")
    public String updateClient(@Valid @ModelAttribute("client") ClientDTO clientDTO, BindingResult bindingResult,
                               Principal principal){

        if (bindingResult.hasErrors()){
            return "profile/edit";
        }
        try {
            String email = principal.getName();
            ClientDTO client = clientService.getClientByEmail(email);
            clientService.updateClient(client.getId(), clientDTO);
        }catch (Exception e){
            bindingResult.rejectValue("email", "validation.email.exists");
            return "profile/edit";
        }
        return "redirect:/profile";
    }


    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/topup")
    public String showAddBalanceForm(){
        return "profile/topup";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/topup")
    public String topUpBalance(@RequestParam("amount") BigDecimal amount,
                               Principal principal,
                               Model model) {
        try {
            clientService.topUpBalance(principal.getName(), amount);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "validation.amount.negative");
            return "profile/topup";
        }

        return "redirect:/books";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/edit")
    public String showEditForm(Model model, Principal principal){
        String email = principal.getName();
        ClientDTO client = clientService.getClientByEmail(email);
        client.setPassword(null);
        model.addAttribute("client", client );
        return "profile/edit";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping()
    public String getMyProfile(Model model, Principal principal){
        String email = principal.getName();
        ClientDTO client = clientService.getClientByEmail(email);
        client.setPassword(null);
        model.addAttribute("client", client);
        return "profile";
    }
}
