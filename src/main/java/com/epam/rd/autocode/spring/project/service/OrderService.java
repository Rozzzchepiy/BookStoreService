package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;

import java.util.*;

public interface OrderService {

    List<OrderDTO> getOrdersByClient(Long id);
    List<OrderDTO> getOrdersByClientEmail(String email);

    List<OrderDTO> getOrdersByEmployee(Long id);

    OrderDTO addOrder(OrderDTO order, String email);
}
