package com.example.boilerroom.model;

import jakarta.persistence.*;

import java.time.LocalDate;

// Representerar ett lån i systemet.
// Innehåller "id", koppling till en bok via "book", "loanDate" och "returnDate" (null tills boken återlämnas).
// "book_id" är unik i databasen vilket säkerställer att en bok bara kan ha ett aktivt lån åt gången.
@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "book_id", unique = true)
    private Book book;

    private LocalDate loanDate;
    private LocalDate returnDate;

    public Loan() {}

    // Getters
    public Long getId() {return id;}
    public Book getBook() {return book;}
    public LocalDate getLoanDate() {return loanDate;}
    public LocalDate getReturnDate() {return returnDate;}

    // Setters
    public void setId(Long id) {this.id = id;}
    public void setBook(Book book) {this.book = book;}
    public void setLoanDate(LocalDate loanDate) {this.loanDate = loanDate;}
    public void setReturnDate(LocalDate returnDate) {this.returnDate = returnDate;}
}
