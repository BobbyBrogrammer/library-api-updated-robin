package com.example.boilerroom.dto;

import java.time.LocalDate;

// LoanDTOs uppgift är att representera ett lån i både request och response.
// Vid request används bara "bookId". I response inkluderas även "id", "bookTitle", "loanDate" och "returnDate".
public class LoanDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private LocalDate loanDate;
    private LocalDate returnDate;

    // Getters
    public Long getId() {return id;}
    public Long getBookId() {return bookId;}
    public String getBookTitle() {return bookTitle;}
    public LocalDate getLoanDate() {return loanDate;}
    public LocalDate getReturnDate() {return returnDate;}

    // Setters
    public void setId(Long id) {this.id = id;}
    public void setBookId(Long bookId) {this.bookId = bookId;}
    public void setBookTitle(String bookTitle) {this.bookTitle = bookTitle;}
    public void setLoanDate(LocalDate loanDate) {this.loanDate = loanDate;}
    public void setReturnDate(LocalDate returnDate) {this.returnDate = returnDate;}
}
