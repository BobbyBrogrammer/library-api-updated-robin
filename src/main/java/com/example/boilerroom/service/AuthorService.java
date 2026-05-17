package com.example.boilerroom.service;

import com.example.boilerroom.dto.AuthorDTO;
import com.example.boilerroom.dto.BookResponse;
import com.example.boilerroom.exception.BookNotFoundException;
import com.example.boilerroom.model.Author;
import com.example.boilerroom.repository.AuthorRepository;
import com.example.boilerroom.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

//import java.util.List;
import java.util.Optional;

// Ansvarar för logiken kring författare och hämtar data från AuthorRepository.
// Stream API används för att mappa en författares böcker till BookResponse när man hämtar böcker per författare.
@Service
public class AuthorService {

    private final AuthorRepository repository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository repository, BookRepository bookRepository) {
        this.repository = repository;
        this.bookRepository = bookRepository;
    }

    public AuthorDTO create(AuthorDTO dto) {
        Author author = new Author();
        author.setName(dto.getName());
        Author saved = repository.save(author);

        AuthorDTO response = new AuthorDTO();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setBookCount(0);
        return response;
    }

    public AuthorDTO getById(Long id) {
        Optional<Author> optional = repository.findById(id);
        if (optional.isEmpty()) {
            throw new BookNotFoundException(id);
        }

        Author author = optional.get();
        AuthorDTO response = new AuthorDTO();
        response.setId(author.getId());
        response.setName(author.getName());
        response.setBookCount(author.getBooks() != null ? author.getBooks().size() : 0);
        return response;
    }

    public Page<BookResponse> getBooksByAuthorId(Long id, Pageable pageable) {
        if (!repository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        return bookRepository.findByAuthorId(id, pageable)
                .map(book -> {
                    BookResponse response = new BookResponse();
                    response.setId(book.getId());
                    response.setTitle(book.getTitle());
                    response.setAuthor(book.getAuthor() != null ? book.getAuthor().getName() : null);
                    response.setIsbn(book.getIsbn());
                    response.setPublishedYear(book.getPublishedYear());
                    return response;
                });
    }

// First method, commenting out to see whats changed when implementing Pagination
//    public List<BookResponse> getBooksByAuthorId(Long id) {
//        Optional<Author> optional = repository.findById(id);
//        if (optional.isEmpty()) {
//            throw new BookNotFoundException(id);
//        }
//
//        Author author = optional.get();
//        return author.getBooks().stream()
//                .map(book -> {
//                    BookResponse response = new BookResponse();
//                    response.setId(book.getId());
//                    response.setTitle(book.getTitle());
//                    response.setAuthor(author.getName());
//                    response.setIsbn(book.getIsbn());
//                    response.setPublishedYear(book.getPublishedYear());
//                    return response;
//                })
//                .toList();
//    }

}
