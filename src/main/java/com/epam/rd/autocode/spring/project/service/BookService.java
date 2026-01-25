package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;

import java.util.List;

public interface BookService {

    List<BookDTO> getAllBooks();

    BookDTO updateBook(Long id, BookDTO book);

    void deleteBook(Long id);

    BookDTO addBook(BookDTO book);

    BookDTO getBookById(Long id);
}
