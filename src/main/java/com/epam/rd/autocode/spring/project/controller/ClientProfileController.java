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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ClientProfileController {
    private final ClientService clientService;

    @PostMapping("/edit")
    public String updateClient(@Valid @ModelAttribute("client") ClientDTO clientDTO,
                               BindingResult bindingResult,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }

        try {
            ClientDTO currentUser = clientService.getClientByEmail(principal.getName());

            clientDTO.setBalance(null);

            clientService.updateClient(currentUser.getId(), clientDTO);

            redirectAttributes.addAttribute("msg", "profile.update.success");
        } catch (Exception e) {
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

    @PostMapping("/topup")
    public String topUpBalance(@RequestParam(value = "amount", required = false) BigDecimal amount,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (amount == null) {
            redirectAttributes.addAttribute("error", "validation.required");
            return "redirect:/profile/topup";
        }

        try {
            clientService.topUpBalance(principal.getName(), amount);
            redirectAttributes.addAttribute("msg", "profile.topup.success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", "validation.amount.negative");
            return "redirect:/profile/topup";
        }

        return "redirect:/profile";
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
