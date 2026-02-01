package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@SessionAttributes("cart")
public class CartController {

    private List<BookItemDTO> bookItemDTOList =  new ArrayList<>();

    public void addBook(Long bookId, String bookName, BigDecimal price, Integer quantity) {
        for (BookItemDTO item : bookItemDTOList) {
            if (item.getBookId().equals(bookId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        bookItemDTOList.add(new BookItemDTO(bookId, bookName, quantity, price));
    }

    public List<BookItemDTO> getItems() {
        return bookItemDTOList;
    }

    public void clear() {
        bookItemDTOList.clear();
    }

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (BookItemDTO bookItemDTO : bookItemDTOList) {
            BigDecimal tmp = bookItemDTO.getPrice().multiply(BigDecimal.valueOf(bookItemDTO.getQuantity()));
            totalPrice = totalPrice.add(tmp);
        }
        return totalPrice;
    }

    public void updateQuantity(Long bookId, Integer newQuantity) {
        if (newQuantity <= 0) {
            removeItem(bookId);
            return;
        }

        for (BookItemDTO item : bookItemDTOList) {
            if (item.getBookId().equals(bookId)) {
                item.setQuantity(newQuantity);
                return;
            }
        }
    }

    public void removeItem(Long bookId) {
        bookItemDTOList.removeIf(item -> item.getBookId().equals(bookId));
    }

}
