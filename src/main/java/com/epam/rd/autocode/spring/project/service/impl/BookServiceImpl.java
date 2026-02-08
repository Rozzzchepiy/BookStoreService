package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.annotation.Loggable;
import com.epam.rd.autocode.spring.project.criteria.BookSearchRequest;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.spec.BookSpecification;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<BookDTO> getAllBooks(BookSearchRequest request) {
        Specification<Book> spec = BookSpecification.filterBooks(request);

        return bookRepository.findAll(spec, request.getPageable())
                .map(book -> modelMapper.map(book, BookDTO.class));
    }

    @Override
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    public List<String> getAllAuthors() {
        return bookRepository.findAllAuthors();
    }

    @Override
    public List<String> getAllGenres() {
        return bookRepository.findAllGenres();
    }

    @Override
    public List<Language> getAllLanguages() {
        return List.of(Language.values());
    }

    @Override
    public List<AgeGroup> getAllAgeGroups() {
        return List.of(AgeGroup.values());
    }

    @Override
    @Loggable
    @Transactional
    public BookDTO updateBook(Long id, BookDTO book) {
        Book updateBook = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        modelMapper.map(book, updateBook);
        updateBook.setId(id);

        Book updatedBook = bookRepository.save(updateBook);

        return modelMapper.map(updatedBook, BookDTO.class);
    }

    @Override
    @Loggable
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found"));
        bookRepository.delete(book);
    }

    @Override
    @Loggable
    @Transactional
    public BookDTO addBook(BookDTO book) {
        Book newBook = modelMapper.map(book, Book.class);
        newBook.setId(null);
        Book updatedBook = bookRepository.save(newBook);
        return modelMapper.map(updatedBook, BookDTO.class);
    }
}