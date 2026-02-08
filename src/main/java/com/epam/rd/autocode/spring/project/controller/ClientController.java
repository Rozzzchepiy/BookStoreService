package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.criteria.ClientSearchRequest;
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
    public String getAllClients(Model model, ClientSearchRequest request) {
        Page<ClientDTO> clientPage = clientService.getAllClients(request);

        model.addAttribute("clients", clientPage);
        model.addAttribute("filter", request);

        return "clients";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/{id}")
    public String getClientById(@PathVariable("id") Long id, Model model){
        ClientDTO clientDTO = clientService.getClientById(id);
        model.addAttribute("client", clientDTO);
        return "clients/client";
    }


    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteClientById(@PathVariable("id") Long id,
                                   ClientSearchRequest request,
                                   RedirectAttributes redirectAttributes) {
        try {
            clientService.deleteClient(id);
            redirectAttributes.addAttribute("msg", "client.delete.success");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addAttribute("error", "error.client.delete_constraint");
        }

        redirectAttributes.addAttribute("page", request.getPage());
        if (request.getKeyword() != null) {
            redirectAttributes.addAttribute("keyword", request.getKeyword());
        }

        return "redirect:/clients";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/{id}/status")
    public String updateClientStatus(@PathVariable("id") Long id,
                                     @RequestParam("blocked") boolean blocked,
                                     ClientSearchRequest request,
                                     RedirectAttributes redirectAttributes) {
        if (blocked) {
            clientService.blockClient(id);
        } else {
            clientService.unblockClient(id);
        }
        redirectAttributes.addAttribute("page", request.getPage());
        if (request.getKeyword() != null) {
            redirectAttributes.addAttribute("keyword", request.getKeyword());
        }
        return "redirect:/clients";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/{id}/block")
    public String blockClientFromDetails(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            clientService.blockClient(id);
            redirectAttributes.addAttribute("msg", "client.block.success");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "common.error");
        }
        return "redirect:/clients/" + id;
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/{id}/unblock")
    public String unblockClientFromDetails(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            clientService.unblockClient(id);
            redirectAttributes.addAttribute("msg", "client.unblock.success");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "common.error");
        }
        return "redirect:/clients/" + id;
    }


}
