package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.criteria.BookSearchRequest;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookService {

    List<String> getAllAuthors();
    List<String> getAllGenres();
    List<Language> getAllLanguages();
    List<AgeGroup> getAllAgeGroups();

    BookDTO updateBook(Long id, BookDTO book);

    void deleteBook(Long id);

    BookDTO addBook(BookDTO book);

    Page<BookDTO> getAllBooks(BookSearchRequest request);

    BookDTO getBookById(Long id);
}
