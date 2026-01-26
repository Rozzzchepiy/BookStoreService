package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO{

    private Long id;

    @NotBlank
    private String name;
    @NotBlank
    private String genre;
    @NotBlank
    private AgeGroup ageGroup;

    private BigDecimal price;

    private LocalDate publicationDate;

    @NotBlank
    private String author;
    private Integer pages;

    @NotBlank
    private String characteristics;
    @NotBlank
    private String description;
    private Language language;
}
