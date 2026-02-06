package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {

    private Long id;

    @NotBlank(message = "{validation.required}")
    private String name;

    @NotBlank(message = "{validation.required}")
    private String genre;

    @NotNull(message = "{validation.required}")
    private AgeGroup ageGroup;

    @NotNull(message = "{validation.required}")
    @Min(value = 0, message = "{validation.price.min}")
    private BigDecimal price;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "{validation.date.past}")
    @NotNull(message = "{validation.required}")
    private LocalDate publicationDate;

    @NotBlank(message = "{validation.required}")
    private String author;

    @NotNull(message = "{validation.required}")
    @Min(value = 0, message = "{validation.price.min}")
    private Integer pages;

    @NotBlank(message = "{validation.required}")
    private String characteristics;

    @NotBlank(message = "{validation.required}")
    private String description;

    @NotNull(message = "{validation.required}")
    private Language language;
}