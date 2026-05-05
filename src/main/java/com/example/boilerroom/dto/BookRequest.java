package com.example.boilerroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// BookRequests uppgift är att representera inkommande data när en ny bok skapas via POST /api/v1/books.
// Innehåller "title" (obligatorisk), valfritt "authorId", "isbn" och "publishedYear".
public class BookRequest {

    @Schema(description = "Title of the book")
    @NotBlank(message = "Title is required")
    private String title;


    @Schema(description = "Author ID")
    private Long authorId;

    @Schema(description = "ISBN number")
    private String isbn;

    @Schema(description = "Year the book was published")
    private int publishedYear;

    // Getters
    public String getTitle() {return title;}
    public Long getAuthorId() {return authorId;}
    public String getIsbn() {return isbn;}
    public int getPublishedYear() {return publishedYear;}

    // Setters
    public void setTitle(String title) {this.title = title;}
    public void setAuthorId(Long authorId) {this.authorId = authorId;}
    public void setIsbn(String isbn) {this.isbn = isbn;}
    public void setPublishedYear(int publishedYear) {this.publishedYear = publishedYear;}
}
