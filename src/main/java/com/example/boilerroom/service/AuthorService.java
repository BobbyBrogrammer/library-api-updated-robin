package com.example.boilerroom.service;

import com.example.boilerroom.dto.AuthorDTO;
import com.example.boilerroom.dto.BookResponse;
import com.example.boilerroom.exception.BookNotFoundException;
import com.example.boilerroom.model.Author;
import com.example.boilerroom.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Ansvarar för logiken kring författare och hämtar data från AuthorRepository.
// Stream API används för att mappa en författares böcker till BookResponse när man hämtar böcker per författare.
@Service
public class AuthorService {

    private final AuthorRepository repository;

    public AuthorService(AuthorRepository repository) {
        this.repository = repository;
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

    public List<BookResponse> getBooksByAuthorId(Long id) {
        Optional<Author> optional = repository.findById(id);
        if (optional.isEmpty()) {
            throw new BookNotFoundException(id);
        }

        Author author = optional.get();
        return author.getBooks().stream()
                .map(book -> {
                    BookResponse response = new BookResponse();
                    response.setId(book.getId());
                    response.setTitle(book.getTitle());
                    response.setAuthor(author.getName());
                    response.setIsbn(book.getIsbn());
                    response.setPublishedYear(book.getPublishedYear());
                    return response;
                })
                .toList();
    }

}
