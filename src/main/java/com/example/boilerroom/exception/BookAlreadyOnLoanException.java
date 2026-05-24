package com.example.boilerroom.exception;

// Kastas när en bok redan är utlånad och någon försöker låna den igen
// Fångas av GlobalExceptionHandler och returnerar HTTP 400 Bad Request
public class BookAlreadyOnLoanException extends RuntimeException {
    public BookAlreadyOnLoanException(Long bookId) {
        super("Book with ID " + bookId + " is already on loan");
    }
}
