package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;

import java.util.*;

public interface OrderService {

    List<OrderDTO> getAllOrders();
    List<OrderDTO> getOrdersByClient(Long id);
    List<OrderDTO> getOrdersByClientEmail(String email);
    List<OrderDTO> getOrdersByEmployeeEmail(String email);
    List<OrderDTO> getOrdersByEmployee(Long id);
    OrderDTO addOrder(OrderDTO order, String email);
    List<OrderDTO> getOrdersByStatus(OrderStatus status);

    OrderDTO getOrderById(Long id);
    OrderDTO updateStatus(Long id, OrderStatus status, String email);

    OrderDTO takeOrder(Long id, String currentUsername);
}
