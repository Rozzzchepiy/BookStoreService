package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.criteria.BookSearchRequest;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;
    private final CartService cartService;

    @GetMapping
    public String showAllBooks(Model model, BookSearchRequest request) {
        Page<BookDTO> booksPage = bookService.getAllBooks(request);

        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("currentPage", booksPage.getNumber());
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("totalItems", booksPage.getTotalElements());

        populateReferenceData(model);

        model.addAttribute("filter", request);

        return "books/list";
    }

    @GetMapping("/{id}")
    public String getBookDetails(@PathVariable("id") Long id, Model model, Principal principal){
        var book = bookService.getBookById(id);
        model.addAttribute("book",book);
        boolean isBookInCart = false;
        if (principal != null) {
            try {
                isBookInCart = cartService.isBookInCart(principal.getName(), id);
            } catch (NotFoundException e) {
                isBookInCart = false;
            }
        }

        model.addAttribute("isBookInCart", isBookInCart);

        return "books/details";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id")  Long id, Model model){
        BookDTO bookDTO = bookService.getBookById(id);
        model.addAttribute("book",bookDTO);
        return  "books/edit";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/{id}/buy-intent")
    public String buyBookAfterLogin(@PathVariable("id") Long id, Principal principal) {
        cartService.addItemToCart(principal.getName(), id, 1);
        return "redirect:/orders/basket";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/add")
    public String showAddForm(Model model){
        model.addAttribute("bookDTO",new BookDTO());
        return "books/add";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute BookDTO book, BindingResult bindingResult, RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()){
            return "books/add";
        }
        bookService.addBook(book);
        redirectAttributes.addAttribute("msg", "book.add.success");
        return "redirect:/books";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes){
        try {
            bookService.deleteBook(id);
            redirectAttributes.addAttribute("msg", "book.delete.success");
            return "redirect:/books";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addAttribute("error", "book.delete.error.integrity");
            return "redirect:/books/" + id;
        }
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable("id") Long id, @Valid @ModelAttribute("book") BookDTO book,  BindingResult bindingResult, RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()){
            return "books/edit";
        }
        bookService.updateBook(id, book);
        redirectAttributes.addAttribute("msg", "book.update.success");
        return "redirect:/books/" + id;
    }

    private void populateReferenceData(Model model) {
        model.addAttribute("allAuthors", bookService.getAllAuthors());
        model.addAttribute("allGenres", bookService.getAllGenres());
        model.addAttribute("allLanguages", bookService.getAllLanguages());
        model.addAttribute("allAgeGroups", bookService.getAllAgeGroups());
    }

}
