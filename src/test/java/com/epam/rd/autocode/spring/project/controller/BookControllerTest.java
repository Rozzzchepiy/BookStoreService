package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        bookDTO = new BookDTO();
        bookDTO.setId(1L);
        bookDTO.setName("Test Book");
        bookDTO.setAuthor("Test Author");
        bookDTO.setGenre("Fiction");
        bookDTO.setPrice(new BigDecimal("100.00"));
        bookDTO.setPublicationDate(LocalDate.now());
        bookDTO.setLanguage(Language.ENGLISH);
        bookDTO.setAgeGroup(AgeGroup.ADULT);
        bookDTO.setPages(300);
        bookDTO.setCharacteristics("Hardcover");
        bookDTO.setDescription("Description");
    }

    @Test
    @DisplayName("GET /books should return list view with attributes")
    void showAllBooks_ShouldReturnListView() throws Exception {
        Page<BookDTO> booksPage = new PageImpl<>(List.of(bookDTO));

        when(bookService.getAllBooks(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(booksPage);
        when(bookService.getAllAuthors()).thenReturn(List.of("Author"));
        when(bookService.getAllGenres()).thenReturn(List.of("Genre"));
        when(bookService.getAllLanguages()).thenReturn(List.of(Language.values()));
        when(bookService.getAllAgeGroups()).thenReturn(List.of(AgeGroup.values()));

        mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "12")
                        .param("sortField", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books", "currentPage", "totalPages", "totalItems"))
                .andExpect(model().attributeExists("allAuthors", "allGenres", "allLanguages", "allAgeGroups"));
    }

    @Test
    @DisplayName("GET /books/{id} should return details view")
    void getBookDetails_ShouldReturnDetailsView() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(bookDTO);

        mockMvc.perform(get("/books/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("books/details"))
                .andExpect(model().attribute("book", bookDTO));
    }

    @Test
    @DisplayName("GET /books/add should return add view")
    void showAddForm_ShouldReturnAddView() throws Exception {
        mockMvc.perform(get("/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/add"))
                .andExpect(model().attributeExists("bookDTO"));
    }

    @Test
    @DisplayName("POST /books/add success should redirect")
    void addBook_Success() throws Exception {
        mockMvc.perform(post("/books/add")
                        .with(csrf())
                        .param("name", "New Book")
                        .param("author", "Author")
                        .param("genre", "Genre")
                        .param("price", "50.00")
                        .param("publicationDate", LocalDate.now().minusDays(1).toString())
                        .param("language", "ENGLISH")
                        .param("ageGroup", "ADULT")
                        .param("pages", "200")
                        .param("characteristics", "Softcover")
                        .param("description", "Desc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).addBook(any(BookDTO.class));
    }

    @Test
    @DisplayName("POST /books/add validation error should return add view")
    void addBook_ValidationError() throws Exception {
        // Sending empty name/genre/price to trigger validation errors
        mockMvc.perform(post("/books/add")
                        .with(csrf())
                        .param("name", "")
                        .param("price", "-10.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/add"))
                .andExpect(model().hasErrors());

        verify(bookService, never()).addBook(any(BookDTO.class));
    }

    @Test
    @DisplayName("GET /books/edit/{id} should return edit view")
    void showEditForm_ShouldReturnEditView() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(bookDTO);

        mockMvc.perform(get("/books/edit/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("books/edit"))
                .andExpect(model().attribute("book", bookDTO));
    }

    @Test
    @DisplayName("POST /books/edit/{id} success should redirect")
    void updateBook_Success() throws Exception {
        mockMvc.perform(post("/books/edit/{id}", 1L)
                        .with(csrf())
                        .param("id", "1")
                        .param("name", "Updated Book")
                        .param("author", "Updated Author")
                        .param("genre", "Genre")
                        .param("price", "150.00")
                        .param("publicationDate", LocalDate.now().minusDays(1).toString())
                        .param("language", "ENGLISH")
                        .param("ageGroup", "ADULT")
                        .param("pages", "350")
                        .param("characteristics", "Hardcover")
                        .param("description", "Updated Desc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).updateBook(eq(1L), any(BookDTO.class));
    }

    @Test
    @DisplayName("POST /books/edit/{id} validation error should return edit view")
    void updateBook_ValidationError() throws Exception {
        mockMvc.perform(post("/books/edit/{id}", 1L)
                        .with(csrf())
                        .param("name", "")) // Invalid
                .andExpect(status().isOk())
                .andExpect(view().name("books/edit"))
                .andExpect(model().hasErrors());

        verify(bookService, never()).updateBook(anyLong(), any(BookDTO.class));
    }

    @Test
    @DisplayName("POST /books/delete/{id} should redirect")
    void deleteBook_Success() throws Exception {
        mockMvc.perform(post("/books/delete/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).deleteBook(1L);
    }
}