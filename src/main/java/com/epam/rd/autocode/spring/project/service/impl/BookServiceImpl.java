package com.epam.rd.autocode.spring.project.service.impl;

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
    public BookDTO getBookByName(String name) {
        Book book = bookRepository.findByNameIgnoreCase(name)
                .orElseThrow(()-> new NotFoundException("Book not found"));

        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    @Transactional
    public BookDTO updateBookByName(String name, BookDTO book) {
        Book updateBook = bookRepository.findByNameIgnoreCase(name)
                .orElseThrow(()-> new NotFoundException("Book not found"));

        modelMapper.map(book, updateBook);
        updateBook.setName(name);

        Book updatedBook = bookRepository.save(updateBook);

        return  modelMapper.map(updatedBook, BookDTO.class);
    }

    @Override
    @Transactional
    public void deleteBookByName(String name) {
        Book book = bookRepository.findByNameIgnoreCase(name)
                .orElseThrow(()-> new NotFoundException("Book not found"));
        bookRepository.delete(book);
    }

    @Override
    @Transactional
    public BookDTO addBook(BookDTO book) {
        Book newBook = modelMapper.map(book, Book.class);
        Book updatedBook = bookRepository.save(newBook);
        return  modelMapper.map(updatedBook, BookDTO.class);
    }
}
