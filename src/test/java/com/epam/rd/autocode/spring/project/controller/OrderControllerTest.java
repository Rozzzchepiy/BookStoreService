package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.component.BalanceHelper;
import com.epam.rd.autocode.spring.project.component.CartComponent;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private BookService bookService;

    @MockBean
    private CartComponent cart;

    @MockBean
    private UserRepository userRepository;

    @MockBean(name = "balanceHelper")
    private BalanceHelper balanceHelper;

    private OrderDTO orderDTO;
    private User user;
    private final String TEST_EMAIL = "user@test.com";

    @BeforeEach
    void setUp() {
        orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setClientEmail(TEST_EMAIL);
        orderDTO.setPrice(new BigDecimal("100.00"));
        orderDTO.setStatus(OrderStatus.NEW);

        user = new User();
        user.setId(10L);
        user.setEmail(TEST_EMAIL);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allOrders_ShouldReturnOrdersView() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(List.of(orderDTO));
        when(orderService.getFilteredOrders(any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/orders/admin/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders", "pageTitle"));
    }

    @Test
    void myClientOrders_ShouldReturnOrdersView() throws Exception {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        Page<OrderDTO> page = new PageImpl<>(List.of(orderDTO));
        when(orderService.getFilteredOrders(eq(10L), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/orders/client/my")
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));
    }

    @Test
    void myEmployeeOrders_ShouldReturnOrdersView() throws Exception {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        Page<OrderDTO> page = new PageImpl<>(List.of(orderDTO));
        when(orderService.getFilteredOrders(any(), eq(10L), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/orders/employee/my")
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("pageTitle", "Мої замовлення"));
    }

    @Test
    void availableOrders_ShouldReturnOrdersView() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(List.of(orderDTO));
        when(orderService.getFilteredOrders(any(), any(), any(), eq(List.of(OrderStatus.NEW)), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/orders/available"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("pageTitle", "Доступні замовлення"));
    }

    @Test
    void takeOrder_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/orders/{id}/take", 1L)
                        .with(csrf())
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/available"));

        verify(orderService).takeOrder(1L, TEST_EMAIL);
    }

    @Test
    void refuseOrder_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/orders/{id}/refuse", 1L)
                        .with(csrf())
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/employee/my"));

        verify(orderService).updateStatus(1L, OrderStatus.NEW, TEST_EMAIL);
    }

    @Test
    void canceledOrder_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/orders/{id}/canceled", 1L)
                        .with(csrf())
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/employee/my"));

        verify(orderService).refund(1L, TEST_EMAIL);
    }

    @Test
    void deliverOrder_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/orders/{id}/deliver", 1L)
                        .with(csrf())
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/employee/my"));

        verify(orderService).deliver(1L, TEST_EMAIL);
    }

    @Test
    void canceledOrderByClient_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/orders/{id}/client/canceled", 1L)
                        .with(csrf())
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/client/my"));

        verify(orderService).refund(1L, TEST_EMAIL);
    }

    @Test
    void getAllOrdersByEmployee_ShouldReturnOrdersView() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(List.of(orderDTO));
        when(orderService.getFilteredOrders(any(), eq(5L), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/orders/employee/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("pageTitle", "Замовлення працівника #5"));
    }

    @Test
    void getAllOrderByClient_ShouldReturnOrdersView() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(List.of(orderDTO));
        when(orderService.getFilteredOrders(eq(5L), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/orders/client/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("pageTitle", "Замовлення клієнта #5"));
    }

    @Test
    void getOrderDetails_AccessAllowed_Owner() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderDTO);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(TEST_EMAIL, "pw", List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/orders/{id}/details", 1L)
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("order-details"))
                .andExpect(model().attribute("order", orderDTO));
    }

    @Test
    void getOrderDetails_AccessAllowed_Admin() throws Exception {
        orderDTO.setClientEmail("other@test.com");
        when(orderService.getOrderById(1L)).thenReturn(orderDTO);
        TestingAuthenticationToken auth = new TestingAuthenticationToken("admin", "pw", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/orders/{id}/details", 1L)
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("order-details"));
    }

    @Test
    void getOrderDetails_AccessDenied() throws Exception {
        orderDTO.setClientEmail("other@test.com");
        when(orderService.getOrderById(1L)).thenReturn(orderDTO);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(TEST_EMAIL, "pw", List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            mockMvc.perform(get("/orders/{id}/details", 1L)
                            .principal(auth))
                    .andExpect(status().isForbidden());
        } catch (Exception e) {
            if (!(e.getCause() instanceof AccessDeniedException)) {
                throw e;
            }
        }
    }

    @Test
    void addToBasket_ShouldRedirect() throws Exception {
        BookDTO book = new BookDTO();
        book.setId(5L);
        book.setName("Book");
        book.setPrice(BigDecimal.TEN);
        when(bookService.getBookById(5L)).thenReturn(book);

        mockMvc.perform(post("/orders/basket/add")
                        .with(csrf())
                        .param("bookId", "5")
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/5"));

        verify(cart).addBook(5L, "Book", BigDecimal.TEN, 2);
    }

    @Test
    void updateBasketQuantity_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/orders/basket/update")
                        .with(csrf())
                        .param("bookId", "5")
                        .param("quantity", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket"));

        verify(cart).updateQuantity(5L, 3);
    }

    @Test
    void removeFromBasket_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/orders/basket/remove")
                        .with(csrf())
                        .param("bookId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket"));

        verify(cart).removeItem(5L);
    }

    @Test
    void showBasket_ShouldReturnBasketView() throws Exception {
        when(cart.getItems()).thenReturn(Collections.emptyList());
        when(cart.getTotalPrice()).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/orders/basket"))
                .andExpect(status().isOk())
                .andExpect(view().name("basket"))
                .andExpect(model().attributeExists("items", "totalPrice"));
    }

    @Test
    void createOrder_Success() throws Exception {
        BookItemDTO item = new BookItemDTO();
        when(cart.getItems()).thenReturn(List.of(item));

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/client/my"));

        verify(orderService).addOrder(any(OrderDTO.class), eq(TEST_EMAIL));
        verify(cart).clear();
    }

    @Test
    void createOrder_EmptyBasket() throws Exception {
        when(cart.getItems()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket?error=empty"));

        verify(orderService, never()).addOrder(any(), any());
    }

    @Test
    void createOrder_ServiceException() throws Exception {
        BookItemDTO item = new BookItemDTO();
        when(cart.getItems()).thenReturn(List.of(item));
        doThrow(new RuntimeException("Balance low")).when(orderService).addOrder(any(), eq(TEST_EMAIL));

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket?error=Balance low"));
    }

    @Test
    void addOrder_Success() throws Exception {
        mockMvc.perform(post("/orders/add")
                        .with(csrf())
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket"));

        verify(orderService).addOrder(any(OrderDTO.class), eq(TEST_EMAIL));
    }

    @Test
    void addOrder_ValidationErrors() throws Exception {
        mockMvc.perform(post("/orders/add")
                        .with(csrf())
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().is3xxRedirection());
    }
}