package com.epam.rd.autocode.spring.project.criteria;

import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrderSearchRequest {
    private Integer page = 0;
    private Integer size = 10;

    private String sortField = "orderDate";
    private String sortDir = "desc";

    private String search;
    private List<OrderStatus> statuses;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Long clientId;
    private Long employeeId;

    public Pageable getPageable() {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        return PageRequest.of(pageNum, pageSize, sort);
    }

    public String getReverseSortDir() {
        return "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc";
    }
}