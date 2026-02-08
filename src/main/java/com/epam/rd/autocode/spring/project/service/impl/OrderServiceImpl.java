package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.annotation.Loggable;
import com.epam.rd.autocode.spring.project.criteria.OrderSearchRequest;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.spec.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;


    @Override
    @Loggable
    @Transactional
    public OrderDTO addOrder(OrderDTO orderDTO, String email) {
        User client = getUserByEmail(email);
        if (!client.getRoles().contains(Role.CLIENT)) {
            throw new AccessDeniedException("Only clients can create orders");
        }

        Order newOrder = new Order();
        newOrder.setStatus(OrderStatus.NEW);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setClient(client);

        BigDecimal totalPrice = createOrderItems(orderDTO, newOrder);
        newOrder.setPrice(totalPrice);

        processPayment(client, totalPrice);

        return mapToDto(orderRepository.save(newOrder));
    }


    @Override
    public OrderDTO getOrderById(Long id) {
        return mapToDto(getOrderEntity(id));
    }

    @Override
    @Loggable
    @Transactional
    public OrderDTO updateStatus(Long id, OrderStatus status, String email) {
        Order order = getOrderEntity(id);

        boolean isEmployee = order.getEmployee() != null && Objects.equals(order.getEmployee().getEmail(), email);
        boolean isClient = Objects.equals(order.getClient().getEmail(), email);

        if (!isEmployee && !isClient) {
            throw new AccessDeniedException("This order is not yours");
        }

        if (status == OrderStatus.NEW) {
            order.setEmployee(null);
        }
        order.setStatus(status);

        return mapToDto(orderRepository.save(order));
    }

    @Override
    @Loggable
    @Transactional
    public OrderDTO takeOrder(Long id, String currentUsername) {
        Order order = getOrderEntity(id);

        if (order.getEmployee() != null) {
            throw new AccessDeniedException("This order was taken by another employee");
        }

        User employee = getUserByEmail(currentUsername);
        order.setEmployee(employee);
        order.setStatus(OrderStatus.ASSIGNED);

        return mapToDto(orderRepository.save(order));
    }

    @Override
    public Page<OrderDTO> getFilteredOrders(OrderSearchRequest request) {
        Specification<Order> spec = OrderSpecification.filterOrders(request);
        return orderRepository.findAll(spec, request.getPageable())
                .map(this::mapToDto);
    }

    @Override
    @Loggable
    @Transactional
    public void refund(Long id, String currentUsername) {
        Order order = getOrderEntity(id);
        User currentUser = getUserByEmail(currentUsername);

        if (currentUser.getRoles().contains(Role.CLIENT) && !order.getClient().equals(currentUser)) {
            throw new AccessDeniedException("You cannot cancel someone else's order");
        }

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order cannot be cancelled in current status");
        }

        User clientToRefund = order.getClient();
        BigDecimal refundAmount = order.getPrice();

        var profile = clientToRefund.getClientProfile();
        profile.setBalance(profile.getBalance().add(refundAmount));

        order.setStatus(OrderStatus.CANCELLED);

        userRepository.save(clientToRefund);
        orderRepository.save(order);
    }

    @Override
    @Loggable
    @Transactional
    public void deliver(Long id, String currentUsername) {
        Order order = getOrderEntity(id);
        User user = getUserByEmail(currentUsername);

        if (order.getEmployee() == null || !order.getEmployee().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not assigned to this order");
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    private Order getOrderEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    private BigDecimal createOrderItems(OrderDTO orderDTO, Order newOrder) {
        List<BookItem> itemsList = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        if (orderDTO.getBookItems() != null) {
            for (BookItemDTO itemDTO : orderDTO.getBookItems()) {
                Book book = bookRepository.findById(itemDTO.getBookId())
                        .orElseThrow(() -> new NotFoundException("Book not found"));

                BigDecimal itemTotal = book.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                totalPrice = totalPrice.add(itemTotal);

                BookItem bookItem = new BookItem();
                bookItem.setBook(book);
                bookItem.setQuantity(itemDTO.getQuantity());
                bookItem.setOrder(newOrder);

                itemsList.add(bookItem);
            }
        }
        newOrder.setBookItems(itemsList);
        return totalPrice;
    }

    private void processPayment(User client, BigDecimal amount) {
        BigDecimal currentBalance = client.getClientProfile().getBalance();
        if (currentBalance.compareTo(amount) < 0) {
            throw new RuntimeException("error.not_enough_money");
        }
        client.getClientProfile().setBalance(currentBalance.subtract(amount));
        userRepository.save(client);
    }

    private OrderDTO mapToDto(Order order) {
        OrderDTO dto = modelMapper.map(order, OrderDTO.class);

        List<BookItemDTO> items = order.getBookItems().stream()
                .map(item -> new BookItemDTO(
                        item.getBook().getId(),
                        item.getBook().getName(),
                        item.getQuantity(),
                        item.getBook().getPrice()))
                .collect(Collectors.toList());
        dto.setBookItems(items);

        if (order.getClient() != null) dto.setClientEmail(order.getClient().getEmail());
        if (order.getEmployee() != null) dto.setEmployeeEmail(order.getEmployee().getEmail());

        return dto;
    }



}
