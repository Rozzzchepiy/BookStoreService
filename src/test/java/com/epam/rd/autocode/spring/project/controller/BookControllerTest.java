package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.criteria.BookSearchRequest;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.security.Principal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void showAllBooks_ShouldReturnListView() throws Exception {
        Page<BookDTO> page = new PageImpl<>(Collections.emptyList());
        when(bookService.getAllBooks(any(BookSearchRequest.class))).thenReturn(page);
        when(bookService.getAllAuthors()).thenReturn(Collections.emptyList());
        when(bookService.getAllGenres()).thenReturn(Collections.emptyList());
        when(bookService.getAllLanguages()).thenReturn(Collections.emptyList());
        when(bookService.getAllAgeGroups()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books", "currentPage", "totalPages", "totalItems", "filter"));
    }

    @Test
    void getBookDetails_ShouldReturnDetailsView_WhenPrincipalNull() throws Exception {
        Long id = 1L;
        BookDTO bookDTO = new BookDTO();
        when(bookService.getBookById(id)).thenReturn(bookDTO);

        mockMvc.perform(get("/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("books/details"))
                .andExpect(model().attribute("book", bookDTO))
                .andExpect(model().attribute("isBookInCart", false));
    }

    @Test
    void getBookDetails_ShouldReturnDetailsView_WhenPrincipalNotNullAndBookInCart() throws Exception {
        Long id = 1L;
        BookDTO bookDTO = new BookDTO();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user");
        when(bookService.getBookById(id)).thenReturn(bookDTO);
        when(cartService.isBookInCart("user", id)).thenReturn(true);

        mockMvc.perform(get("/books/{id}", id).principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("books/details"))
                .andExpect(model().attribute("isBookInCart", true));
    }

    @Test
    void getBookDetails_ShouldHandleNotFoundException_FromCartService() throws Exception {
        Long id = 1L;
        BookDTO bookDTO = new BookDTO();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user");
        when(bookService.getBookById(id)).thenReturn(bookDTO);
        when(cartService.isBookInCart("user", id)).thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(get("/books/{id}", id).principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("books/details"))
                .andExpect(model().attribute("isBookInCart", false));
    }

    @Test
    void showEditForm_ShouldReturnEditView() throws Exception {
        Long id = 1L;
        BookDTO bookDTO = new BookDTO();
        when(bookService.getBookById(id)).thenReturn(bookDTO);

        mockMvc.perform(get("/books/edit/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("books/edit"))
                .andExpect(model().attribute("book", bookDTO));
    }

    @Test
    void buyBookAfterLogin_ShouldAddToCartAndRedirect() throws Exception {
        Long id = 1L;
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user");

        mockMvc.perform(get("/books/{id}/buy-intent", id).principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket"));

        verify(cartService).addItemToCart("user", id, 1);
    }

    @Test
    void showAddForm_ShouldReturnAddView() throws Exception {
        mockMvc.perform(get("/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/add"))
                .andExpect(model().attributeExists("bookDTO"));
    }

    @Test
    void addBook_ShouldRedirectOnSuccess() throws Exception {
        // Передаємо всі поля, щоб пройти @Valid
        mockMvc.perform(post("/books/add")
                        .param("name", "Valid Book")
                        .param("author", "Test Author")
                        .param("genre", "FICTION")
                        .param("language", "ENGLISH")
                        .param("ageGroup", "ADULT")
                        .param("pages", "100")
                        .param("publicationDate", "2023-01-01")
                        .param("price", "100.00")
                        .param("description", "Desc")
                        .param("characteristics", "Chars"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books?msg=book.add.success"));

        verify(bookService).addBook(any(BookDTO.class));
    }

    @Test
    void addBook_ShouldReturnAddView_WhenValidationFails() throws Exception {
        BookController validationController = new BookController(bookService, cartService) {
            @Override
            public String addBook(BookDTO book, org.springframework.validation.BindingResult bindingResult, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
                bindingResult.rejectValue("name", "error");
                return super.addBook(book, bindingResult, redirectAttributes);
            }
        };
        MockMvc validationMockMvc = MockMvcBuilders.standaloneSetup(validationController)
                .setViewResolvers(new InternalResourceViewResolver("/templates/", ".html"))
                .build();

        mockMvc.perform(post("/books/add")
                        .param("price", "100.00")) // Немає імені -> помилка валідації
                .andExpect(status().isOk())
                .andExpect(view().name("books/add"));

        verify(bookService, never()).addBook(any());
    }

    @Test
    void deleteBook_ShouldRedirectOnSuccess() throws Exception {
        Long id = 1L;
        mockMvc.perform(post("/books/delete/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books?msg=book.delete.success"));

        verify(bookService).deleteBook(id);
    }

    @Test
    void deleteBook_ShouldRedirectWithError_WhenIntegrityViolation() throws Exception {
        Long id = 1L;
        doThrow(new DataIntegrityViolationException("Constraint violation")).when(bookService).deleteBook(id);

        mockMvc.perform(post("/books/delete/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + id + "?error=book.delete.error.integrity"));
    }


    @Test
    void updateBook_ShouldReturnEditView_WhenValidationFails() throws Exception {
        BookController validationController = new BookController(bookService, cartService) {
            @Override
            public String updateBook(Long id, BookDTO book, org.springframework.validation.BindingResult bindingResult, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
                bindingResult.rejectValue("name", "error");
                return super.updateBook(id, book, bindingResult, redirectAttributes);
            }
        };
        MockMvc validationMockMvc = MockMvcBuilders.standaloneSetup(validationController)
                .setViewResolvers(new InternalResourceViewResolver("/templates/", ".html"))
                .build();

        validationMockMvc.perform(post("/books/edit/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("books/edit"));

        verify(bookService, never()).updateBook(anyLong(), any());
    }
}