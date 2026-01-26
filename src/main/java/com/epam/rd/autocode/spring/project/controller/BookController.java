package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public String showAllBooks(Model model){
        var books = bookService.getAllBooks();
        model.addAttribute("books",books);
        return "books/list";
    }

    @GetMapping("/{id}")
    public String getBookDetails(@PathVariable("id") Long id, Model model){
        var book = bookService.getBookById(id);
        model.addAttribute("book",book);
        return "books/details";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id")  Long id, Model model){
        BookDTO bookDTO = bookService.getBookById(id);
        model.addAttribute("book",bookDTO);
        return  "books/edit";
    }

    @GetMapping("/add")
    public String showAddForm(Model model){
        model.addAttribute("bookDTO",new BookDTO());
        return "books/add";
    }

    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute BookDTO book, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "books/add";
        }
        BookDTO newBook = bookService.addBook(book);
        return "redirect:/books";
    }

    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id){
        bookService.deleteBook(id);
        return "redirect:/books";
    }


    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable("id") Long id, @Valid @ModelAttribute BookDTO book,  BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "books/edit";
        }
        bookService.updateBook(id, book);
        return "redirect:/books";
    }

}
