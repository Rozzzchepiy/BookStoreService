package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.criteria.OrderSearchRequest;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotEnoughMoneyException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private OrderServiceImpl orderService;


    @Test
    void addOrder_ShouldCreateOrder_WhenBalanceIsSufficient() {
        String email = "client@test.com";
        User client = createClient(email, new BigDecimal("100.00"));

        OrderDTO inputDto = new OrderDTO();
        BookItemDTO itemDto = new BookItemDTO();
        itemDto.setBookId(1L);
        itemDto.setQuantity(2);
        inputDto.setBookItems(List.of(itemDto));

        Book book = new Book();
        book.setId(1L);
        book.setPrice(new BigDecimal("10.00"));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(new OrderDTO());

        OrderDTO result = orderService.addOrder(inputDto, email);

        assertNotNull(result);
        assertEquals(new BigDecimal("80.00"), client.getClientProfile().getBalance()); // 100 - (10 * 2)
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void addOrder_ShouldThrowAccessDenied_WhenUserNotClient() {
        String email = "admin@test.com";
        User admin = new User();
        admin.setEmail(email);
        admin.setRoles(Set.of(Role.ADMIN));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(admin));

        assertThrows(AccessDeniedException.class, () -> orderService.addOrder(new OrderDTO(), email));
    }

    @Test
    void addOrder_ShouldThrowNotEnoughMoney_WhenBalanceLow() {
        String email = "client@test.com";
        User client = createClient(email, BigDecimal.ZERO);

        OrderDTO inputDto = new OrderDTO();
        BookItemDTO itemDto = new BookItemDTO();
        itemDto.setBookId(1L);
        itemDto.setQuantity(1);
        inputDto.setBookItems(List.of(itemDto));

        Book book = new Book();
        book.setPrice(new BigDecimal("10.00"));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(NotEnoughMoneyException.class, () -> orderService.addOrder(inputDto, email));
    }


    @Test
    void getOrderById_ShouldReturnOrder() {
        Long id = 1L;
        Order order = new Order();
        order.setBookItems(Collections.emptyList());

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(new OrderDTO());

        assertNotNull(orderService.getOrderById(id));
    }

    @Test
    void getOrderById_ShouldThrowNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.getOrderById(1L));
    }


    @Test
    void updateStatus_ShouldUpdate_WhenUserIsClientOwner() {
        Long id = 1L;
        String email = "owner@test.com";
        Order order = new Order();
        User client = new User();
        client.setEmail(email);
        order.setClient(client);
        order.setBookItems(Collections.emptyList());

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(new OrderDTO());

        orderService.updateStatus(id, OrderStatus.CANCELLED, email);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void updateStatus_ShouldThrowAccessDenied_WhenUserNotOwnerOrEmployee() {
        Long id = 1L;
        String email = "stranger@test.com";
        Order order = new Order();
        User client = new User();
        client.setEmail("owner@test.com");
        order.setClient(client);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> orderService.updateStatus(id, OrderStatus.CANCELLED, email));
    }

    @Test
    void updateStatus_ShouldUnassignEmployee_WhenStatusNew() {
        Long id = 1L;
        String email = "emp@test.com";
        Order order = new Order();
        User client = new User();
        client.setEmail("client@test.com");
        User employee = new User();
        employee.setEmail(email);

        order.setClient(client);
        order.setEmployee(employee);
        order.setBookItems(Collections.emptyList());

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(new OrderDTO());

        orderService.updateStatus(id, OrderStatus.NEW, email);

        assertNull(order.getEmployee());
        assertEquals(OrderStatus.NEW, order.getStatus());
    }


    @Test
    void takeOrder_ShouldAssignEmployee() {
        Long id = 1L;
        String email = "emp@test.com";
        Order order = new Order();
        order.setBookItems(Collections.emptyList());

        User employee = new User();
        employee.setEmail(email);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(orderRepository.save(order)).thenReturn(order);
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(new OrderDTO());

        orderService.takeOrder(id, email);

        assertEquals(employee, order.getEmployee());
        assertEquals(OrderStatus.ASSIGNED, order.getStatus());
    }

    @Test
    void takeOrder_ShouldThrow_WhenAlreadyTaken() {
        Long id = 1L;
        Order order = new Order();
        order.setEmployee(new User());

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> orderService.takeOrder(id, "any"));
    }


    @Test
    void refund_ShouldRefundMoneyAndCancelOrder() {
        Long id = 1L;
        String email = "client@test.com";
        User client = createClient(email, new BigDecimal("50.00"));

        Order order = new Order();
        order.setClient(client);
        order.setPrice(new BigDecimal("100.00"));
        order.setStatus(OrderStatus.NEW);
        order.setBookItems(Collections.emptyList()); // Avoid NPE in mapToDto if logged? but method returns void

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(client));

        orderService.refund(id, email);

        assertEquals(new BigDecimal("150.00"), client.getClientProfile().getBalance());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(userRepository).save(client);
    }

    @Test
    void refund_ShouldThrow_WhenOrderDelivered() {
        Long id = 1L;
        String email = "client@test.com";
        User client = createClient(email, BigDecimal.TEN);
        Order order = new Order();
        order.setClient(client);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(client));

        assertThrows(IllegalStateException.class, () -> orderService.refund(id, email));
    }

    @Test
    void refund_ShouldThrow_WhenUserNotOwner() {
        Long id = 1L;
        String email = "other@test.com";
        User other = createClient(email, BigDecimal.ZERO);

        Order order = new Order();
        User owner = new User();
        owner.setId(99L);
        order.setClient(owner);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(other));

        assertThrows(AccessDeniedException.class, () -> orderService.refund(id, email));
    }


    @Test
    void deliver_ShouldSetStatusDelivered() {
        Long id = 1L;
        String email = "emp@test.com";
        User employee = new User();
        employee.setId(10L);
        employee.setEmail(email);

        Order order = new Order();
        order.setEmployee(employee);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(employee));

        orderService.deliver(id, email);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void deliver_ShouldThrow_WhenNotAssignedEmployee() {
        Long id = 1L;
        String email = "emp@test.com";
        User user = new User();
        user.setId(10L);

        Order order = new Order();
        order.setEmployee(null);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> orderService.deliver(id, email));
    }


    @Test
    void getFilteredOrders_ShouldReturnPage() {
        OrderSearchRequest request = mock(OrderSearchRequest.class);
        Pageable pageable = Pageable.unpaged();
        when(request.getPageable()).thenReturn(pageable);

        Order order = new Order();
        order.setBookItems(Collections.emptyList());
        Page<Order> page = new PageImpl<>(List.of(order));

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(new OrderDTO());

        Page<OrderDTO> result = orderService.getFilteredOrders(request);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getMyOrders_ShouldSetClientIdAndCallFindAll() {
        String email = "client@test.com";
        User client = new User();
        client.setId(100L);

        OrderSearchRequest request = new OrderSearchRequest();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        orderService.getMyOrders(email, request);

        assertEquals(100L, request.getClientId());
        verify(orderRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getMyWorkOrders_ShouldSetEmployeeIdAndCallFindAll() {
        String email = "emp@test.com";
        User employee = new User();
        employee.setId(200L);

        OrderSearchRequest request = new OrderSearchRequest();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        orderService.getMyWorkOrders(email, request);

        assertEquals(200L, request.getEmployeeId());
        verify(orderRepository).findAll(any(Specification.class), any(Pageable.class));
    }


    private User createClient(String email, BigDecimal balance) {
        User user = new User();
        user.setEmail(email);
        user.setRoles(Set.of(Role.CLIENT));
        ClientProfile profile = new ClientProfile();
        profile.setBalance(balance);
        user.setClientProfile(profile);
        return user;
    }
}