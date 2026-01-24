package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(()->new NotFoundException("Client not found"));

        if (!client.getRoles().contains(Role.CLIENT)){
            throw new NotFoundException("This user is not a client");
        }

        return orderRepository.findAllByClient(client).stream()
                .map(this::mapper)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(()->new NotFoundException("Employee not found"));

        if (!employee.getRoles().contains(Role.EMPLOYEE)){
            throw new NotFoundException("This user is not a employee");
        }

        return orderRepository.findAllByEmployee(employee).stream()
                .map(this::mapper)
                .collect(Collectors.toList());


    }

    @Override
    @Transactional
    public OrderDTO addOrder(OrderDTO order) {

        User client = userRepository.findByEmail(order.getClientEmail())
                .orElseThrow(()->new NotFoundException("Client not found"));
        if (!client.getRoles().contains(Role.CLIENT)){
            throw new NotFoundException("This user is not a client");
        }

        User employee = null;
        if (order.getEmployeeEmail() != null){
            employee = userRepository.findByEmail(order.getEmployeeEmail())
                    .orElseThrow(() -> new NotFoundException("Employee not found"));
        }

        if (!employee.getRoles().contains(Role.EMPLOYEE)) {
            throw new NotFoundException("This user is not an employee");
        }

        Order newOrder = new Order();
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setClient(client);
        newOrder.setEmployee(employee);


        List<BookItem> itemsList = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        if (order.getBookItems() != null){
            for (BookItemDTO bookItemDTO : order.getBookItems()) {
                Book book = bookRepository.findByNameIgnoreCase(bookItemDTO.getBookName())
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

    private OrderDTO mapper (Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderDate(order.getOrderDate());
        orderDTO.setPrice(order.getPrice());
        if(order.getClient() != null){
            orderDTO.setClientEmail(order.getClient().getEmail());
        }
        if(orderDTO.getEmployeeEmail() != null){
            orderDTO.setEmployeeEmail(order.getEmployee().getEmail());
        }

        List<BookItemDTO> items =order.getBookItems().stream()
                .map(item -> new BookItemDTO(item.getBook().getName(), item.getQuantity()))
                .collect(Collectors.toList());

        orderDTO.setBookItems(items);
        return orderDTO;
    }

}
