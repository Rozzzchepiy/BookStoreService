package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.service.OrderService;
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
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/basket")
    public  String getAllMyOrders(Model model, Principal principal){
        String email = principal.getName();
        List<OrderDTO> orders = orderService.getOrdersByClientEmail(email);
        model.addAttribute("orders", orders);
        return "orders";
    }
    
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("client/{id}")
    public String getOrderByClient(@PathVariable("id") Long id, Model model){
        List<OrderDTO> orders = orderService.getOrdersByClient(id);
        model.addAttribute("orders", orders);
        return "orders";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee/{id}")
    public String getAllOrdersByEmployee(@PathVariable("id") Long id, Model model){
        List<OrderDTO> orders = orderService.getOrdersByEmployee(id);
        model.addAttribute("orders", orders);
        return "orders";
    }
    @PostMapping("/add")
    public String addOrder(@Valid @ModelAttribute("orderDTO") OrderDTO orderDTO, BindingResult result, Principal principal){
        if (result.hasErrors()) {
            return "orders";
        }
        orderService.addOrder(orderDTO, principal.getName());
        return "redirect:/orders/basket";
    }
}
