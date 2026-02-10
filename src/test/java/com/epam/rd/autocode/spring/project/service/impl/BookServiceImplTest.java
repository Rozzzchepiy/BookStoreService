package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.criteria.BookSearchRequest;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BookServiceImpl bookService;


    @Test
    void getAllBooks_ShouldReturnPageOfBookDTOs() {
        BookSearchRequest request = mock(BookSearchRequest.class);
        Pageable pageable = mock(Pageable.class);
        when(request.getPageable()).thenReturn(pageable);

        Book book = new Book();
        BookDTO bookDTO = new BookDTO();
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(bookPage);
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        Page<BookDTO> result = bookService.getAllBooks(request);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(bookDTO, result.getContent().get(0));
        verify(bookRepository).findAll(any(Specification.class), eq(pageable));
    }


    @Test
    void getBookById_ShouldReturnBookDTO_WhenBookExists() {
        Long id = 1L;
        Book book = new Book();
        BookDTO bookDTO = new BookDTO();

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        BookDTO result = bookService.getBookById(id);
        // Assert
        assertEquals(bookDTO, result);
        verify(bookRepository).findById(id);
    }

    @Test
    void getBookById_ShouldThrowNotFoundException_WhenBookDoesNotExist() {
        Long id = 1L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.getBookById(id));
        verify(bookRepository).findById(id);
    }


    @Test
    void getAllAuthors_ShouldReturnListOfStrings() {
        List<String> authors = List.of("Author 1", "Author 2");
        when(bookRepository.findAllAuthors()).thenReturn(authors);

        List<String> result = bookService.getAllAuthors();

        assertEquals(authors, result);
        verify(bookRepository).findAllAuthors();
    }


    @Test
    void getAllGenres_ShouldReturnListOfStrings() {
        List<String> genres = List.of("Genre 1", "Genre 2");
        when(bookRepository.findAllGenres()).thenReturn(genres);

        List<String> result = bookService.getAllGenres();

        assertEquals(genres, result);
        verify(bookRepository).findAllGenres();
    }


    @Test
    void getAllLanguages_ShouldReturnAllEnumValues() {
        List<Language> result = bookService.getAllLanguages();

        assertEquals(List.of(Language.values()), result);
        assertEquals(Language.values().length, result.size());
    }


    @Test
    void getAllAgeGroups_ShouldReturnAllEnumValues() {
        List<AgeGroup> result = bookService.getAllAgeGroups();

        assertEquals(List.of(AgeGroup.values()), result);
        assertEquals(AgeGroup.values().length, result.size());
    }


    @Test
    void updateBook_ShouldUpdateAndReturnBookDTO_WhenBookExists() {
        Long id = 1L;
        BookDTO inputDto = new BookDTO();
        Book existingBook = new Book();
        Book updatedBook = new Book();
        BookDTO resultDto = new BookDTO();
        when(bookRepository.findById(id)).thenReturn(Optional.of(existingBook));
        doNothing().when(modelMapper).map(inputDto, existingBook);
        when(bookRepository.save(existingBook)).thenReturn(updatedBook);
        when(modelMapper.map(updatedBook, BookDTO.class)).thenReturn(resultDto);
        BookDTO result = bookService.updateBook(id, inputDto);
        assertEquals(resultDto, result);
        verify(modelMapper).map(inputDto, existingBook);
        assertEquals(id, existingBook.getId());
        verify(bookRepository).save(existingBook);
    }

    @Test
    void updateBook_ShouldThrowNotFoundException_WhenBookDoesNotExist() {
        Long id = 1L;
        BookDTO inputDto = new BookDTO();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.updateBook(id, inputDto));
        verify(bookRepository, never()).save(any());
    }


    @Test
    void deleteBook_ShouldDeleteBook_WhenBookExists() {
        Long id = 1L;
        Book book = new Book();
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        bookService.deleteBook(id);

        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_ShouldThrowNotFoundException_WhenBookDoesNotExist() {
        Long id = 1L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.deleteBook(id));
        verify(bookRepository, never()).delete((Book) any());
    }


    @Test
    void addBook_ShouldSaveAndReturnBookDTO() {
        BookDTO inputDto = new BookDTO();
        Book mappedBook = new Book();
        mappedBook.setId(999L);

        Book savedBook = new Book();
        BookDTO resultDto = new BookDTO();

        when(modelMapper.map(inputDto, Book.class)).thenReturn(mappedBook);
        when(bookRepository.save(mappedBook)).thenReturn(savedBook);
        when(modelMapper.map(savedBook, BookDTO.class)).thenReturn(resultDto);

        BookDTO result = bookService.addBook(inputDto);

        assertEquals(resultDto, result);

        assertNull(mappedBook.getId());
        verify(bookRepository).save(mappedBook);
    }
}