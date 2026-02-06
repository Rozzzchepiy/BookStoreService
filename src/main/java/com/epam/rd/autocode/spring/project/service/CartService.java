package com.epam.rd.autocode.spring.project.service;


import com.epam.rd.autocode.spring.project.dto.BookItemDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    void addItemToCart(String userEmail, Long bookId, Integer quantity);
    void updateQuantity(String userEmail, Long bookId, Integer newQuantity);
    void removeItem(String userEmail, Long bookId);
    void clearCart(String userEmail);
    List<BookItemDTO> getCartItems(String userEmail);
    BigDecimal getTotalPrice(String userEmail);
}
