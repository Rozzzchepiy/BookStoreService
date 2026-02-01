package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface BookService {

    List<String> getAllAuthors();
    List<String> getAllGenres();
    List<Language> getAllLanguages();

    BookDTO updateBook(Long id, BookDTO book);

    void deleteBook(Long id);

    BookDTO addBook(BookDTO book);

    Page<BookDTO> getAllBooks(String search, List<String> authors, List<String> genres,
                              List<Language> languages, BigDecimal minPrice, BigDecimal maxPrice,
                              org.springframework.data.domain.Pageable pageable);

    BookDTO getBookById(Long id);
}
