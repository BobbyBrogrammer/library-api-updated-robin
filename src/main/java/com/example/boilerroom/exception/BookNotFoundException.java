package com.example.boilerroom.exception;

// Kastas när en bok med ett givet id inte hittas i databasen.
// Fångas av GlobalExceptionHandler och returnerar HTTP 404 Not Found.
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) {
        super("Book with id " + id + " not found");
    }
}
