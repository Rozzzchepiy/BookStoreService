package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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

    public String addBook(@RequestBody BookDTO book){
        BookDTO newBook = bookService.addBook(book);
        return "redirect:/books";
    }

}
