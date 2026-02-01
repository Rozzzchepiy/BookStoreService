package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.annotation.Loggable;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Book not found"));

        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    @Loggable
    @Transactional
    public BookDTO updateBook(Long id, BookDTO book) {
        Book updateBook = bookRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Book not found"));

        modelMapper.map(book, updateBook);
        updateBook.setId(id);

        Book updatedBook = bookRepository.save(updateBook);

        return  modelMapper.map(updatedBook, BookDTO.class);
    }

    @Override
    @Loggable
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Book not found"));
        bookRepository.delete(book);
    }

    @Override
    @Loggable
    @Transactional
    public BookDTO addBook(BookDTO book) {
        Book newBook = modelMapper.map(book, Book.class);
        newBook.setId(null);
        Book updatedBook = bookRepository.save(newBook);
        return  modelMapper.map(updatedBook, BookDTO.class);
    }
}
