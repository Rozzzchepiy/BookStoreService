package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.annotation.Loggable;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapper).collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByClient(Long id) {
        User client = userRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Client not found"));


        return orderRepository.findAllByClient(client).stream()
                .map(this::mapper)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByClientEmail(String email) {
        User client = userRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("Client not found"));

        return orderRepository.findAllByClient(client).stream()
                .map(this::mapper)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(Long id) {
        User employee = userRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Employee not found"));


        return orderRepository.findAllByEmployee(employee).stream()
                .map(this::mapper)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByEmployeeEmail(String email) {
        User employee = userRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("Employee not found"));

        if (!employee.getRoles().contains(Role.EMPLOYEE)){
            throw new NotFoundException("This user is not a employee");
        }

        return orderRepository.findAllByEmployee(employee).stream()
                .map(this::mapper)
                .collect(Collectors.toList());

    }

    @Override
    @Loggable
    @Transactional
    public OrderDTO addOrder(OrderDTO order, String email) {

        User client = userRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("Client not found"));
        if (!client.getRoles().contains(Role.CLIENT)){
            throw new NotFoundException("This user is not a client");
        }


        Order newOrder = new Order();
        newOrder.setStatus(OrderStatus.NEW);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setClient(client);
        newOrder.setEmployee(null);


        List<BookItem> itemsList = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        if (order.getBookItems() != null){
            for (BookItemDTO bookItemDTO : order.getBookItems()) {
                Book book = bookRepository.findById(bookItemDTO.getBookId())
                        .orElseThrow(() -> new NotFoundException("Book not found"));

                BigDecimal itemTotal = book.getPrice().multiply(BigDecimal.valueOf(bookItemDTO.getQuantity()));
                totalPrice = totalPrice.add(itemTotal);

                BookItem bookItem = new BookItem();
                bookItem.setBook(book);
                bookItem.setQuantity(bookItemDTO.getQuantity());
                bookItem.setOrder(newOrder);

                itemsList.add(bookItem);
            }
        }
        newOrder.setBookItems(itemsList);
        newOrder.setPrice(totalPrice);

        BigDecimal currentBalance = client.getClientProfile().getBalance();

        if (currentBalance.compareTo(totalPrice) < 0) {
            throw new RuntimeException("Not enough money! Required: " + totalPrice + ", Available: " + currentBalance);
        }

        client.getClientProfile().setBalance(currentBalance.subtract(totalPrice));
        userRepository.save(client);

        Order savedOrder = orderRepository.save(newOrder);

        return mapper(savedOrder);

    }

    @Override
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findAllByStatus(status).stream().map(this::mapper).collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return mapper(order);
    }

    @Override
    @Loggable
    @Transactional
    public OrderDTO updateStatus(Long id, OrderStatus status, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        boolean isEmployee = order.getEmployee() != null && Objects.equals(order.getEmployee().getEmail(), email);
        boolean isClient = Objects.equals(order.getClient().getEmail(), email);

        if (!isEmployee && !isClient) {
            throw new AccessDeniedException("This order is not yours");
        }
        if (status == OrderStatus.NEW) {
            order.setEmployee(null);
        }
        order.setStatus(status);
        orderRepository.save(order);
        return mapper(order);
    }

    @Override
    @Loggable
    @Transactional
    public OrderDTO takeOrder(Long id, String currentUsername) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if  (order.getEmployee() != null) {
            throw new AccessDeniedException("This order was taken by another employee");
        }

        User user = userRepository.findByEmail(currentUsername).orElseThrow(() -> new NotFoundException("User not found"));

        order.setEmployee(user);

        order.setStatus(OrderStatus.ASSIGNED);
        orderRepository.save(order);

        return mapper(order);
    }

    @Override
    @Loggable
    @Transactional
    public void refund(Long id, String currentUsername) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (currentUser.getRoles().contains(Role.CLIENT)) {
            if (!order.getClient().getEmail().equals(currentUsername)) {
                throw new org.springframework.security.access.AccessDeniedException("Ви не можете скасувати чуже замовлення");
            }
        }

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Замовлення не можна скасувати, оскільки воно вже виконане або скасоване");
        }

        BigDecimal refundAmount = order.getPrice();

        var profile = order.getClient().getClientProfile();

        BigDecimal currentBalance = profile.getBalance();
        BigDecimal newBalance = currentBalance.add(refundAmount);

        profile.setBalance(newBalance);

        order.setStatus(OrderStatus.CANCELLED);

        userRepository.save(currentUser);
        orderRepository.save(order);
    }

    private OrderDTO mapper (Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderDate(order.getOrderDate());
        orderDTO.setPrice(order.getPrice());
        orderDTO.setId(order.getId());
        if(order.getClient() != null){
            orderDTO.setClientEmail(order.getClient().getEmail());
        }
        if(order.getEmployee() != null){
            orderDTO.setEmployeeEmail(order.getEmployee().getEmail());
        }
        orderDTO.setStatus(order.getStatus());

        List<BookItemDTO> items =order.getBookItems().stream()
                .map(item -> new BookItemDTO(item.getBook().getId(), item.getBook().getName(), item.getQuantity(), item.getBook().getPrice()))
                .collect(Collectors.toList());

        orderDTO.setBookItems(items);
        return orderDTO;
    }

}
