package com.epam.rd.autocode.spring.project.criteria;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class ClientSearchRequest {
    private int page = 0;
    private int size = 5;
    private String keyword;

    public Pageable getPageable() {
        return PageRequest.of(page, size, Sort.by("id").descending());
    }
}