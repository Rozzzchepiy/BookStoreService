package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    public String registerClient(@Valid @ModelAttribute("client") ClientDTO clientDTO,  BindingResult result,  Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        try{
            clientService.addClient(clientDTO);
        }catch(Exception ex){
            result.rejectValue("email", "validation.email.exists");
            return  "register";
        }

        return "redirect:/login";
    }

}
