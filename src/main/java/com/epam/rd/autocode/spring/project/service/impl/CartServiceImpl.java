package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.annotation.Loggable;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.CartItem;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.CartItemRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;


    @Override
    @Loggable
    @Transactional
    public void addItemToCart(String userEmail, Long bookId, Integer quantity) {
        User user = getUser(userEmail);
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndBookId(user, bookId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new NotFoundException("Book not found"));
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setBook(book);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    @Override
    @Loggable
    @Transactional
    public void updateQuantity(String userEmail, Long bookId, Integer newQuantity) {
        User user = getUser(userEmail);
        if (newQuantity <= 0) {
            cartItemRepository.deleteByUserAndBookId(user, bookId);
        } else {
            CartItem item = cartItemRepository.findByUserAndBookId(user, bookId)
                    .orElseThrow(() -> new NotFoundException("Item not found"));
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        }
    }

    @Override
    @Loggable
    @Transactional
    public void removeItem(String userEmail, Long bookId) {
        User user = getUser(userEmail);
        cartItemRepository.deleteByUserAndBookId(user, bookId);
    }

    @Override
    @Loggable
    @Transactional
    public void clearCart(String userEmail) {
        User user = getUser(userEmail);
        cartItemRepository.deleteByUser(user);
    }

    @Override
    public Page<BookItemDTO> getCartItems(String userEmail, Pageable pageable) {
        User user = getUser(userEmail);
        Page<CartItem> cartItemPage = cartItemRepository.findByUser(user, pageable);

        return cartItemPage.map(this::mapToDTO);
    }

    @Override
    public BigDecimal getTotalPrice(String userEmail) {
        User user = getUser(userEmail);
        return cartItemRepository.getTotalPriceByUser(user);
    }

    @Override
    public List<BookItemDTO> getAllCartItems(String userEmail) {
        User user =  getUser(userEmail);
        return cartItemRepository.findByUser(user, Pageable.unpaged()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isBookInCart(String userEmail, Long bookId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return cartItemRepository.findByUserAndBookId(user, bookId).isPresent();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private BookItemDTO mapToDTO(CartItem cartItem) {
        return new BookItemDTO(
                cartItem.getBook().getId(),
                cartItem.getBook().getName(),
                cartItem.getQuantity(),
                cartItem.getBook().getPrice()
        );
    }
}
