package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.CartItemDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
@SessionAttributes("cart")
@RequiredArgsConstructor
public class CartController {
    private final BookService bookService;

    @ModelAttribute("cart")
    public List<CartItemDTO> init(){
        return new ArrayList<>();
    }

    @GetMapping
    public String showCart(@ModelAttribute("cart") List<CartItemDTO> cart, Model model){
        model.addAttribute("cart", cart);
        return "cart";
    }

}
