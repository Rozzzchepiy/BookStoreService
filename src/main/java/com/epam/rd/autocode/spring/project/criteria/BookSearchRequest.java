package com.epam.rd.autocode.spring.project.criteria;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BookSearchRequest {
    private int page = 0;
    private int size = 12;
    private String sortField = "id";
    private String sortDir = "asc";

    private String search;
    private List<String> authors;
    private List<String> genres;
    private List<Language> languages;
    private List<AgeGroup> ageGroups;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;


    public Pageable getPageable() {
        Sort sort = Sort.by(sortField);
        sort = "desc".equalsIgnoreCase(sortDir) ? sort.descending() : sort.ascending();
        return PageRequest.of(page, size, sort);
    }

    public String getReverseSortDir() {
        return "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc";
    }
}