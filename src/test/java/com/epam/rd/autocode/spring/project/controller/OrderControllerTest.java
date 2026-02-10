package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.criteria.OrderSearchRequest;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotEnoughMoneyException;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.CartService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void allOrders_ShouldReturnOrdersView() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(Collections.emptyList());
        when(orderService.getFilteredOrders(any(OrderSearchRequest.class))).thenReturn(page);

        mockMvc.perform(get("/orders/admin/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders", "pageTitle"));
    }

    @Test
    void myClientOrders_ShouldReturnOrdersView() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("client");
        Page<OrderDTO> page = new PageImpl<>(Collections.emptyList());
        when(orderService.getMyOrders(eq("client"), any(OrderSearchRequest.class))).thenReturn(page);

        mockMvc.perform(get("/orders/client/my").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));
    }

    @Test
    void myEmployeeOrders_ShouldReturnOrdersView() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("employee");
        Page<OrderDTO> page = new PageImpl<>(Collections.emptyList());
        when(orderService.getMyWorkOrders(eq("employee"), any(OrderSearchRequest.class))).thenReturn(page);

        mockMvc.perform(get("/orders/employee/my").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("pageTitle"));
    }

    @Test
    void availableOrders_ShouldReturnOrdersView() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(Collections.emptyList());
        when(orderService.getFilteredOrders(any(OrderSearchRequest.class))).thenReturn(page);

        mockMvc.perform(get("/orders/available"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("pageTitle"));
    }

    @Test
    void getAllOrdersByEmployee_ShouldReturnOrdersView() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(Collections.emptyList());
        when(orderService.getFilteredOrders(any(OrderSearchRequest.class))).thenReturn(page);

        mockMvc.perform(get("/orders/employee/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("pageTitle"));
    }

    @Test
    void getAllOrdersByClient_ShouldReturnOrdersView() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(Collections.emptyList());
        when(orderService.getFilteredOrders(any(OrderSearchRequest.class))).thenReturn(page);

        mockMvc.perform(get("/orders/client/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("pageTitle"));
    }


    @Test
    void getOrderDetails_ShouldReturnDetails_WhenAdmin() throws Exception {
        Long id = 1L;
        String email = "admin@test.com";
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(auth).getAuthorities();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail("other@test.com");

        when(orderService.getOrderById(id)).thenReturn(orderDTO);

        mockMvc.perform(get("/orders/{id}/details", id).principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("order-details"));
    }



    @Test
    void addToBasket_ShouldRedirectOnSuccess() throws Exception {
        Long bookId = 1L;
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("client");

        mockMvc.perform(post("/orders/basket/add")
                        .param("bookId", String.valueOf(bookId))
                        .param("quantity", "2")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + bookId + "?msg=basket.add.success"));

        verify(cartService).addItemToCart("client", bookId, 2);
    }

    @Test
    void updateBasketQuantity_ShouldRedirect() throws Exception {
        Long bookId = 1L;
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("client");

        mockMvc.perform(post("/orders/basket/update")
                        .param("bookId", String.valueOf(bookId))
                        .param("quantity", "5")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket?page=0&size=5"));

        verify(cartService).updateQuantity("client", bookId, 5);
    }

    @Test
    void removeFromBasket_ShouldRedirect() throws Exception {
        Long bookId = 1L;
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("client");

        mockMvc.perform(post("/orders/basket/remove")
                        .param("bookId", String.valueOf(bookId))
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket?page=0&size=5&msg=basket.remove.success"));

        verify(cartService).removeItem("client", bookId);
    }

    @Test
    void showBasket_ShouldReturnBasketView() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("client");

        Page<BookItemDTO> page = new PageImpl<>(Collections.emptyList());
        when(cartService.getCartItems(eq("client"), any(Pageable.class))).thenReturn(page);
        when(cartService.getTotalPrice("client")).thenReturn(BigDecimal.TEN);

        mockMvc.perform(get("/orders/basket").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("basket"))
                .andExpect(model().attributeExists("items", "totalPrice"));
    }

    @Test
    void createOrder_ShouldRedirectSuccess_WhenCartNotEmpty() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("client");

        List<BookItemDTO> items = List.of(new BookItemDTO());
        when(cartService.getAllCartItems("client")).thenReturn(items);

        mockMvc.perform(post("/orders/create").principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/client/my?msg=order.create.success"));

        verify(orderService).addOrder(any(OrderDTO.class), eq("client"));
        verify(cartService).clearCart("client");
    }

    @Test
    void createOrder_ShouldRedirectError_WhenCartEmpty() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("client");
        when(cartService.getAllCartItems("client")).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/orders/create").principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket?error=order.create.empty"));

        verify(orderService, never()).addOrder(any(), any());
    }

    @Test
    void createOrder_ShouldRedirectError_WhenNotEnoughMoney() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("client");

        List<BookItemDTO> items = List.of(new BookItemDTO());
        when(cartService.getAllCartItems("client")).thenReturn(items);
        doThrow(new NotEnoughMoneyException("No money")).when(orderService).addOrder(any(OrderDTO.class), eq("client"));

        mockMvc.perform(post("/orders/create").principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket?error=not_enough_money"));
    }
}