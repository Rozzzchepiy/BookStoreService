package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.CartItem;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.CartItemRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CartServiceImpl cartService;


    @Test
    void addItemToCart_ShouldUpdateQuantity_WhenItemExists() {
        String email = "test@email.com";
        Long bookId = 1L;
        Integer quantityToAdd = 2;

        User user = new User();
        user.setEmail(email);

        CartItem existingItem = new CartItem();
        existingItem.setQuantity(5);
        existingItem.setUser(user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserAndBookId(user, bookId)).thenReturn(Optional.of(existingItem));

        cartService.addItemToCart(email, bookId, quantityToAdd);

        assertEquals(7, existingItem.getQuantity());
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void addItemToCart_ShouldCreateNewItem_WhenItemDoesNotExist() {
        String email = "test@email.com";
        Long bookId = 1L;
        Integer quantity = 1;

        User user = new User();
        Book book = new Book();
        book.setId(bookId);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserAndBookId(user, bookId)).thenReturn(Optional.empty());
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        cartService.addItemToCart(email, bookId, quantity);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItemToCart_ShouldThrowNotFoundException_WhenBookNotFound() {
        String email = "test@email.com";
        Long bookId = 1L;

        User user = new User();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserAndBookId(user, bookId)).thenReturn(Optional.empty());
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartService.addItemToCart(email, bookId, 1));
    }

    @Test
    void addItemToCart_ShouldThrowNotFoundException_WhenUserNotFound() {
        String email = "unknown@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartService.addItemToCart(email, 1L, 1));
    }


    @Test
    void updateQuantity_ShouldDeleteItem_WhenNewQuantityIsZeroOrLess() {
        String email = "test@email.com";
        Long bookId = 1L;
        User user = new User();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        cartService.updateQuantity(email, bookId, 0);

        verify(cartItemRepository).deleteByUserAndBookId(user, bookId);
    }

    @Test
    void updateQuantity_ShouldUpdateItem_WhenQuantityIsPositiveAndItemExists() {
        // Arrange
        String email = "test@email.com";
        Long bookId = 1L;
        Integer newQuantity = 10;
        User user = new User();
        CartItem item = new CartItem();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserAndBookId(user, bookId)).thenReturn(Optional.of(item));

        cartService.updateQuantity(email, bookId, newQuantity);

        assertEquals(newQuantity, item.getQuantity());
        verify(cartItemRepository).save(item);
    }

    @Test
    void updateQuantity_ShouldThrowNotFoundException_WhenItemNotFound() {
        String email = "test@email.com";
        Long bookId = 1L;
        User user = new User();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserAndBookId(user, bookId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartService.updateQuantity(email, bookId, 5));
    }


    @Test
    void removeItem_ShouldCallDelete() {
        String email = "test@email.com";
        Long bookId = 1L;
        User user = new User();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        cartService.removeItem(email, bookId);

        verify(cartItemRepository).deleteByUserAndBookId(user, bookId);
    }


    @Test
    void clearCart_ShouldCallDeleteByUser() {
        String email = "test@email.com";
        User user = new User();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        cartService.clearCart(email);

        verify(cartItemRepository).deleteByUser(user);
    }


    @Test
    void getCartItems_ShouldReturnPageOfDTOs() {
        String email = "test@email.com";
        User user = new User();

        Book book = new Book();
        book.setId(100L);
        book.setName("Test Book");
        book.setPrice(BigDecimal.TEN);

        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setQuantity(2);

        Page<CartItem> page = new PageImpl<>(List.of(cartItem));
        Pageable pageable = Pageable.unpaged();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUser(user, pageable)).thenReturn(page);

        Page<BookItemDTO> result = cartService.getCartItems(email, pageable);

        assertEquals(1, result.getTotalElements());
    }


    @Test
    void getTotalPrice_ShouldReturnPrice() {
        String email = "test@email.com";
        User user = new User();
        BigDecimal expectedPrice = new BigDecimal("100.00");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartItemRepository.getTotalPriceByUser(user)).thenReturn(expectedPrice);

        BigDecimal result = cartService.getTotalPrice(email);

        assertEquals(expectedPrice, result);
    }


    @Test
    void getAllCartItems_ShouldReturnListOfDTOs() {
        String email = "test@email.com";
        User user = new User();

        Book book = new Book();
        book.setId(101L);
        book.setName("Another Book");
        book.setPrice(BigDecimal.ONE);

        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setQuantity(1);

        Page<CartItem> page = new PageImpl<>(List.of(cartItem));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUser(user, Pageable.unpaged())).thenReturn(page);

        List<BookItemDTO> result = cartService.getAllCartItems(email);

        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getBookId());
    }


    @Test
    void isBookInCart_ShouldReturnTrue_WhenItemExists() {
        String email = "test@email.com";
        Long bookId = 1L;
        User user = new User();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserAndBookId(user, bookId)).thenReturn(Optional.of(new CartItem()));

        boolean result = cartService.isBookInCart(email, bookId);

        assertTrue(result);
    }

    @Test
    void isBookInCart_ShouldReturnFalse_WhenItemDoesNotExist() {
        String email = "test@email.com";
        Long bookId = 1L;
        User user = new User();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserAndBookId(user, bookId)).thenReturn(Optional.empty());

        boolean result = cartService.isBookInCart(email, bookId);

        assertFalse(result);
    }

    @Test
    void isBookInCart_ShouldThrowNotFoundException_WhenUserNotFound() {
        String email = "unknown@email.com";
        Long bookId = 1L;
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartService.isBookInCart(email, bookId));
    }
}