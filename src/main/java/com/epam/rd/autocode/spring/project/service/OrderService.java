package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.criteria.OrderSearchRequest;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import org.springframework.data.domain.*;


public interface OrderService {

    OrderDTO addOrder(OrderDTO orderDTO, String email);


    OrderDTO getOrderById(Long id);
    OrderDTO updateStatus(Long id, OrderStatus status, String email);

    OrderDTO takeOrder(Long id, String currentUsername);

    void refund(Long id, String currentUsername);

    void deliver(Long id, String currentUsername);
    Page<OrderDTO> getFilteredOrders(OrderSearchRequest request);
}
