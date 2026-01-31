package com.epam.rd.autocode.spring.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemDTO {
    private Long bookId;
    private String title;
    private Integer quantity;
    private BigDecimal price;
}
