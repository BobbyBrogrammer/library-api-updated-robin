package com.example.boilerroom.exception;

// Kastas när en författare med ett givet id inte hittas i databasen
// Fångas av GlobalExceptionHandler och returnerar HTTP 404 Not Found
public class AuthorNotFoundException extends RuntimeException {
    public AuthorNotFoundException(Long id) {
        super("Author with id " + id + " not found");
    }
}
