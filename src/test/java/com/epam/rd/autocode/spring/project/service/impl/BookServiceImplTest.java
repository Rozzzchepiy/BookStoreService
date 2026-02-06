package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private Book book;
    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setName("Test Book");
        book.setAuthor("Test Author");
        book.setGenre("Fantasy");
        book.setPrice(new BigDecimal("100.00"));
        book.setLanguage(Language.ENGLISH);
        book.setAgeGroup(AgeGroup.ADULT);
        book.setPublicationDate(LocalDate.now());

        bookDTO = new BookDTO();
        bookDTO.setId(1L);
        bookDTO.setName("Test Book");
        bookDTO.setAuthor("Test Author");
        bookDTO.setPrice(new BigDecimal("100.00"));
    }


    @Test
    void getAllBooks_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(bookPage);

        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        Page<BookDTO> result = bookService.getAllBooks(
                "search", null, null, null, null, null, null, pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Book");
        verify(bookRepository).findAll(any(Specification.class), eq(pageable));
    }


    @Test
    void getBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        BookDTO result = bookService.getBookById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Book");
    }

    @Test
    void getBookById_NotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Book not found");
    }


    @Test
    void getAllAuthors_Success() {
        when(bookRepository.findAllAuthors()).thenReturn(List.of("Author 1", "Author 2"));

        List<String> result = bookService.getAllAuthors();

        assertThat(result).hasSize(2).contains("Author 1");
    }

    @Test
    void getAllGenres_Success() {
        when(bookRepository.findAllGenres()).thenReturn(List.of("Fantasy", "Sci-Fi"));

        List<String> result = bookService.getAllGenres();

        assertThat(result).hasSize(2).contains("Fantasy");
    }

    @Test
    void getAllLanguages_Success() {
        List<Language> result = bookService.getAllLanguages();

        assertThat(result).isNotEmpty();
        assertThat(result).contains(Language.ENGLISH);
    }

    @Test
    void getAllAgeGroups_Success() {
        List<AgeGroup> result = bookService.getAllAgeGroups();

        assertThat(result).isNotEmpty();
        assertThat(result).contains(AgeGroup.ADULT);
    }


    @Test
    void addBook_Success() {
        BookDTO newBookDTO = new BookDTO();
        newBookDTO.setName("New Book");

        Book mappedBook = new Book();
        mappedBook.setName("New Book");

        Book savedBook = new Book();
        savedBook.setId(10L);
        savedBook.setName("New Book");

        when(modelMapper.map(newBookDTO, Book.class)).thenReturn(mappedBook);

        when(bookRepository.save(mappedBook)).thenReturn(savedBook);

        BookDTO savedDto = new BookDTO();
        savedDto.setId(10L);
        savedDto.setName("New Book");
        when(modelMapper.map(savedBook, BookDTO.class)).thenReturn(savedDto);

        BookDTO result = bookService.addBook(newBookDTO);

        assertThat(result.getId()).isEqualTo(10L);

        assertThat(mappedBook.getId()).isNull();

        verify(bookRepository).save(mappedBook);
    }


    @Test
    void updateBook_NotFound() {
        BookDTO updateInfo = new BookDTO();
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(99L, updateInfo))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Book not found");

        verify(bookRepository, never()).save(any());
    }


    @Test
    void deleteBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_NotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Book not found");

        verify(bookRepository, never()).delete((Book) any());
    }
}