package com.epam.rd.autocode.spring.project.service;


import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    void addItemToCart(String userEmail, Long bookId, Integer quantity);
    void updateQuantity(String userEmail, Long bookId, Integer newQuantity);
    void removeItem(String userEmail, Long bookId);
    void clearCart(String userEmail);
    Page<BookItemDTO> getCartItems(String userEmail, Pageable pageable);
    BigDecimal getTotalPrice(String userEmail);
    List<BookItemDTO> getAllCartItems(String userEmail);
    boolean isBookInCart(String userEmail, Long bookId);
}
