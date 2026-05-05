package com.example.boilerroom.service;

import com.example.boilerroom.dto.BookRequest;
import com.example.boilerroom.dto.BookResponse;
import com.example.boilerroom.dto.BookResponseV2;
import com.example.boilerroom.exception.BookNotFoundException;
import com.example.boilerroom.model.Author;
import com.example.boilerroom.model.Book;
import com.example.boilerroom.repository.AuthorRepository;
import com.example.boilerroom.repository.BookRepository;
import com.example.boilerroom.repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


// BookServices uppgift är att hantera logiken för böcker och hämta data från BookRepository och AuthorRepository.
// LoanRepository används här för att avgöra om en bok är tillgänglig i v2-svaret.
@Service
public class BookService {
    
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.loanRepository = loanRepository;
    }
    public BookResponse create(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setPublishedYear(request.getPublishedYear());

        if (request.getAuthorId() != null) {
            Author author = authorRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new BookNotFoundException(request.getAuthorId()));
            book.setAuthor(author);
        }

        Book saved = bookRepository.save(book);

        BookResponse response = new BookResponse();
        response.setId(saved.getId());
        response.setTitle(saved.getTitle());
        response.setAuthor(saved.getAuthor() != null ? saved.getAuthor().getName() : null);
        response.setIsbn(saved.getIsbn());
        response.setPublishedYear(saved.getPublishedYear());
        return response;
    }

    public List<BookResponse> getAll() {
        List<Book> books = bookRepository.findAll();
        List<BookResponse> responses = new ArrayList<>();

        for (Book book : books) {
            BookResponse response = new BookResponse();
            response.setId(book.getId());
            response.setTitle(book.getTitle());
            response.setAuthor(book.getAuthor() != null ? book.getAuthor().getName() : null);
            response.setIsbn(book.getIsbn());
            response.setPublishedYear(book.getPublishedYear());
            responses.add(response);
        }
        return responses;
    }

    public BookResponse getById(Long id) {
        Optional<Book> bookOptional = bookRepository.findById(id);
        if (bookOptional.isEmpty()) {
            throw new BookNotFoundException(id);
        }
        
        Book book = bookOptional.get();
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor() != null ? book.getAuthor().getName() : null);
        response.setIsbn(book.getIsbn());
        response.setPublishedYear(book.getPublishedYear());
        return response;
    }

    public List<BookResponseV2> getAllV2() {
        List<Book> books = bookRepository.findAll();
        List<BookResponseV2> responses = new ArrayList<>();

        for (Book book : books) {
            BookResponseV2 response = new BookResponseV2();
            response.setId(book.getId());
            response.setTitle(book.getTitle());
            response.setAuthor(book.getAuthor() != null ? book.getAuthor().getName() : null);
            response.setIsbn(book.getIsbn());
            response.setPublishedYear(book.getPublishedYear());
            response.setAvailable(!loanRepository.existsByBookId(book.getId()));
            responses.add(response);
        }
        return responses;
    }

}
