package com.example.boilerroom.exception;

public class BookAlreadyOnLoanException extends RuntimeException {
    public BookAlreadyOnLoanException(Long bookId) {
        super("Book with ID " + bookId + " is already on loan");
    }
}
