package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController { 

    private final ClientService clientService;

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping
    public String getAllClients(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ClientDTO> clientPage = clientService.getAllClients(pageable, keyword);

        model.addAttribute("clients", clientPage);
        model.addAttribute("keyword", keyword);

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
    public String deleteClientById(@PathVariable("id") Long id,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(required = false) String keyword,
                                   RedirectAttributes redirectAttributes) {
        try {
            clientService.deleteClient(id);
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "error.client.delete_constraint");
        }

        redirectAttributes.addAttribute("page", page);
        if (keyword != null && !keyword.isEmpty()) {
            redirectAttributes.addAttribute("keyword", keyword);
        }

        return "redirect:/clients";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/{id}/block")
    public String blockClient(@PathVariable("id") Long id, @RequestHeader(value = "Referer", required = false) String referer) {
        clientService.blockClient(id);
        return "redirect:" + (referer != null ? referer : "/clients");
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/{id}/unblock")
    public String unblockClient(@PathVariable("id") Long id, @RequestHeader(value = "Referer", required = false) String referer) {
        clientService.unblockClient(id);
        return "redirect:" + (referer != null ? referer : "/clients");
    }


}
