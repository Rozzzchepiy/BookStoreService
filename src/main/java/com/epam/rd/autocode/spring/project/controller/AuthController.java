package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final ClientService clientService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/client/register")
    public String register(Model model) {
        model.addAttribute("client", new ClientDTO());
        return "register";
    }

    @PostMapping("/client/register")
    public String register(@ModelAttribute ClientDTO clientDTO) {
        clientService.addClient(clientDTO);
        return "redirect:/login";
    }
}
