package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User client;
    private User employee;
    private Book book;
    private Order order;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setId(1L);
        client.setEmail("client@test.com");
        client.setRoles(new HashSet<>(Collections.singletonList(Role.CLIENT)));
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setBalance(new BigDecimal("100.00"));
        client.setClientProfile(clientProfile);

        employee = new User();
        employee.setId(2L);
        employee.setEmail("employee@test.com");
        employee.setRoles(new HashSet<>(Collections.singletonList(Role.EMPLOYEE)));
        EmployeeProfile employeeProfile = new EmployeeProfile();
        employeeProfile.setId(2L);
        employee.setEmployeeProfile(employeeProfile);

        book = new Book();
        book.setId(10L);
        book.setName("Test Book");
        book.setPrice(new BigDecimal("20.00"));

        order = new Order();
        order.setId(100L);
        order.setClient(client);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.setPrice(new BigDecimal("20.00"));

        BookItem bookItem = new BookItem();
        bookItem.setBook(book);
        bookItem.setQuantity(1);
        bookItem.setOrder(order);
        order.setBookItems(List.of(bookItem));
    }


    @Test
    void getAllOrders_ShouldReturnList() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }


    @Test
    void getOrdersByClient_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(orderRepository.findAllByClient(client)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrdersByClient(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getOrdersByClient_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrdersByClient(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Client not found");
    }

    @Test
    void getOrdersByClientEmail_Success() {
        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(client));
        when(orderRepository.findAllByClient(client)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrdersByClientEmail("client@test.com");

        assertThat(result).hasSize(1);
    }


    @Test
    void getOrdersByEmployee_Success() {
        order.setEmployee(employee);
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(orderRepository.findAllByEmployee(employee)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrdersByEmployee(2L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getOrdersByEmployeeEmail_Success() {
        order.setEmployee(employee);
        when(userRepository.findByEmail("employee@test.com")).thenReturn(Optional.of(employee));
        when(orderRepository.findAllByEmployee(employee)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrdersByEmployeeEmail("employee@test.com");

        assertThat(result).hasSize(1);
    }

    @Test
    void getOrdersByEmployeeEmail_NotAnEmployeeRole() {
        User notEmployee = new User();
        notEmployee.setEmail("fake@test.com");
        notEmployee.setRoles(Set.of(Role.CLIENT));

        when(userRepository.findByEmail("fake@test.com")).thenReturn(Optional.of(notEmployee));

        assertThatThrownBy(() -> orderService.getOrdersByEmployeeEmail("fake@test.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("This user is not a employee");
    }


    @Test
    void addOrder_Success() {
        OrderDTO inputDto = new OrderDTO();
        BookItemDTO itemDto = new BookItemDTO();
        itemDto.setBookId(10L);
        itemDto.setQuantity(2); // Cost 40.00
        inputDto.setBookItems(List.of(itemDto));

        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(101L);
            return o;
        });

        OrderDTO result = orderService.addOrder(inputDto, client.getEmail());

        assertThat(result.getPrice()).isEqualByComparingTo("40.00");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.NEW);

        assertThat(client.getClientProfile().getBalance()).isEqualByComparingTo("60.00");

        verify(userRepository).save(client);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void addOrder_UserNotClientRole() {
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setRoles(Set.of(Role.ADMIN));

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> orderService.addOrder(new OrderDTO(), "admin@test.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("This user is not a client");
    }

    @Test
    void addOrder_BookNotFound() {
        OrderDTO inputDto = new OrderDTO();
        BookItemDTO itemDto = new BookItemDTO();
        itemDto.setBookId(999L);
        inputDto.setBookItems(List.of(itemDto));

        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.addOrder(inputDto, client.getEmail()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Book not found");
    }

    @Test
    void addOrder_InsufficientFunds() {
        OrderDTO inputDto = new OrderDTO();
        BookItemDTO itemDto = new BookItemDTO();
        itemDto.setBookId(10L);
        itemDto.setQuantity(10);
        inputDto.setBookItems(List.of(itemDto));

        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> orderService.addOrder(inputDto, client.getEmail()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not enough money");

        verify(orderRepository, never()).save(any());
    }


    @Test
    void getOrdersByStatus_Success() {
        when(orderRepository.findAllByStatus(OrderStatus.NEW)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrdersByStatus(OrderStatus.NEW);

        assertThat(result).hasSize(1);
    }


    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.getOrderById(100L);

        assertThat(result.getId()).isEqualTo(100L);
    }


    @Test
    void updateStatus_Success_ByEmployee() {
        order.setEmployee(employee);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.updateStatus(100L, OrderStatus.DELIVERED, employee.getEmail());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_Success_ByClient() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.updateStatus(100L, OrderStatus.CANCELLED, client.getEmail());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void updateStatus_AccessDenied() {
        order.setEmployee(employee);
        User otherUser = new User();
        otherUser.setEmail("hacker@test.com");

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(100L, OrderStatus.DELIVERED, "hacker@test.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("This order is not yours");
    }

    @Test
    void updateStatus_SetToNew_ShouldNullifyEmployee() {
        order.setEmployee(employee);
        order.setStatus(OrderStatus.ASSIGNED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.updateStatus(100L, OrderStatus.NEW, employee.getEmail());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(order.getEmployee()).isNull();
    }


    @Test
    void takeOrder_Success() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));

        OrderDTO result = orderService.takeOrder(100L, employee.getEmail());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(order.getEmployee()).isEqualTo(employee);
    }

    @Test
    void takeOrder_AlreadyTaken() {
        order.setEmployee(new User());
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.takeOrder(100L, employee.getEmail()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("This order was taken by another employee");
    }


    @Test
    void refund_Success() {
        order.setPrice(new BigDecimal("50.00"));
        client.getClientProfile().setBalance(new BigDecimal("10.00"));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));

        orderService.refund(100L, client.getEmail());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(client.getClientProfile().getBalance()).isEqualByComparingTo("60.00");

        verify(userRepository).save(client);
        verify(orderRepository).save(order);
    }

    @Test
    void refund_ClientTryToRefundOthersOrder_AccessDenied() {
        User otherClient = new User();
        otherClient.setEmail("other@test.com");
        otherClient.setRoles(Set.of(Role.CLIENT));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherClient));

        assertThatThrownBy(() -> orderService.refund(100L, "other@test.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Ви не можете скасувати чуже замовлення");
    }

    @Test
    void refund_AlreadyDelivered_IllegalState() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> orderService.refund(100L, client.getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("вже виконане або скасоване");
    }


    @Test
    void deliver_Success() {
        order.setEmployee(employee);
        order.setStatus(OrderStatus.ASSIGNED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));

        orderService.deliver(100L, employee.getEmail());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository).save(order);
    }

    @Test
    void deliver_WrongEmployee_AccessDenied() {
        order.setEmployee(employee);

        User wrongEmployee = new User();
        wrongEmployee.setId(3L);
        wrongEmployee.setEmail("wrong@test.com");
        wrongEmployee.setEmployeeProfile(new EmployeeProfile());

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.of(wrongEmployee));

        assertThatThrownBy(() -> orderService.deliver(100L, "wrong@test.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("error.order.access_denied");
    }


    @Test
    void getFilteredOrders_ShouldReturnPage() {
        Pageable pageable = Pageable.unpaged();
        Page<Order> page = new PageImpl<>(List.of(order));

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<OrderDTO> result = orderService.getFilteredOrders(
                1L, 2L, "search", List.of(OrderStatus.NEW), null, null, null, null, pageable
        );

        assertThat(result.getContent()).hasSize(1);
    }
}