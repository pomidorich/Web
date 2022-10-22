package com.example.web.controllers;

import com.example.web.entities.Book;
import com.example.web.repos.BookRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.HttpSession;

@Controller
public class BooksController {
    private final BookRepo bookRepo;

    public BooksController(BookRepo bookRepo) {
        this.bookRepo = bookRepo;
    }

    @GetMapping("/")
    public String start() {
        return "redirect:/signin";
    }

    @GetMapping("/shop")
    public String mainPage(@RequestParam Integer page,
                           HttpSession session, Map<String, Object> model) {
        Pageable paginator = PageRequest.of(page-1, 5);

        Iterable<Book> books = bookRepo.findAll(paginator);

        Iterable<Book> booksAll = bookRepo.findAll();
        List<Book> booksList = new ArrayList<Book>();
        booksAll.forEach(booksList::add);
        int count = booksList.size();
        int ost = count % 5;

        if (ost > 0)
            count = count / 5 + 1;
        else
            count = count / 5;

        List<Integer> pages = new ArrayList<>();
        for(int i = 0; i < count; i++)
            pages.add(i+1);

        model.put("books", books);
        model.put("pages", pages);
        model.put("number", page);
        model.put("count", count);
        model.put("session", session);

        return "main";
    }

    @GetMapping("/create")
    public String addBookForm(Map<String, Object> model, HttpSession session) {
        if (session.getAttribute("role") == null)
            return "redirect:/signin";

        Book book = new Book();
        book.setName("");
        book.setAuthor("");
        book.setPrice(0);
        model.put("book", book);
        model.put("session", session);

        return "create";
    }

    @PostMapping("/create")
    public String addBook(@RequestParam String name,
                          @RequestParam String author,
                          @RequestParam Integer price,
                          Map<String, Object> model, HttpSession session) {
        Book book = new Book(name, author, price);
        bookRepo.save(book);
        model.put("session", session);

        return "redirect:/shop?page=1";
    }

    @GetMapping("/edit")
    public String editBookForm(@RequestParam Integer id,
                               Map<String, Object> model, HttpSession session) {
        if (session.getAttribute("role") == "superuser") {
            Optional<Book> book = bookRepo.findById(id);

            if (book.isPresent()) {
                model.put("book", book.get());
                model.put("session", session);

                return "edit";
            }
        }

        return "redirect:/shop?page=1";
    }

    @PostMapping("/edit")
    public String editBook(@RequestParam Integer id,
                           @RequestParam String name,
                           @RequestParam String author,
                           @RequestParam Integer price,
                           Map<String, Object> model, HttpSession session) {
        Book book = bookRepo.findById(id).get();
        book.setName(name);
        book.setAuthor(author);
        book.setPrice(price);
        bookRepo.save(book);
        model.put("session", session);

        return "redirect:/shop?page=1";
    }

    @GetMapping("/delete")
    public String deleteBook(@RequestParam Integer id, HttpSession session) {
        if (session.getAttribute("role") == "superuser")
            if (bookRepo.findById(id).isPresent())
                bookRepo.deleteById(id);

        return "redirect:/shop?page=1";
    }
}
