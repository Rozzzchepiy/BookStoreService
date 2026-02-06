package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public String showAllBooks(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> authors,
            @RequestParam(required = false) List<String> genres,
            @RequestParam(required = false) List<Language> languages,
            @RequestParam(required = false) List<AgeGroup> ageGroups,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BookDTO> booksPage = bookService.getAllBooks(search, authors, genres, languages, ageGroups, minPrice, maxPrice, pageable);

        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("currentPage", booksPage.getNumber());
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("totalItems", booksPage.getTotalElements());

        model.addAttribute("allAuthors", bookService.getAllAuthors());
        model.addAttribute("allGenres", bookService.getAllGenres());
        model.addAttribute("allLanguages", bookService.getAllLanguages());
        model.addAttribute("allAgeGroups", bookService.getAllAgeGroups());


        model.addAttribute("search", search);
        model.addAttribute("selectedAuthors", authors);
        model.addAttribute("selectedGenres", genres);
        model.addAttribute("selectedLanguages", languages);
        model.addAttribute("selectedAgeGroups", ageGroups);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("size", size);

        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "books/list";
    }

    @GetMapping("/{id}")
    public String getBookDetails(@PathVariable("id") Long id, Model model){
        var book = bookService.getBookById(id);
        model.addAttribute("book",book);
        return "books/details";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id")  Long id, Model model){
        BookDTO bookDTO = bookService.getBookById(id);
        model.addAttribute("book",bookDTO);
        return  "books/edit";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/add")
    public String showAddForm(Model model){
        model.addAttribute("bookDTO",new BookDTO());
        return "books/add";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute BookDTO book, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "books/add";
        }
        bookService.addBook(book);
        return "redirect:/books";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id){
        bookService.deleteBook(id);
        return "redirect:/books";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable("id") Long id, @Valid @ModelAttribute("book") BookDTO book,  BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "books/edit";
        }
        bookService.updateBook(id, book);
        return "redirect:/books";
    }

}
