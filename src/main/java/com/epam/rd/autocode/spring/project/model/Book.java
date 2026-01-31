package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="books")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",  nullable = false)
    private String name;

    @Column(name = "genre",  nullable = false)
    private String genre;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group")
    private AgeGroup  ageGroup;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "publication_year",  nullable = false)
    private LocalDate publicationDate;

    @Column(name = "author")
    private String author;

    @Column(name = "number_of_pages")
    private Integer pages;

    @Column(name = "characteristics")
    private String characteristics;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    private Language language;


}
