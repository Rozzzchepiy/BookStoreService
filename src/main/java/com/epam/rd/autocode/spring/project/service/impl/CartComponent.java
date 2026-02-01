package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.annotation.Loggable;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@SessionAttributes("cart")
public class CartComponent {

    private List<BookItemDTO> bookItemDTOList =  new ArrayList<>();

    @Loggable
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

    @Loggable
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

    @Loggable
    public void removeItem(Long bookId) {
        bookItemDTOList.removeIf(item -> item.getBookId().equals(bookId));
    }

}
