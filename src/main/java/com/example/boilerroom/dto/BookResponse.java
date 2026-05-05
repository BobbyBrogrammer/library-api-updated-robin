package com.example.boilerroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// BookResponses uppgift är att representera en bok i API-svaret (v1).
// Innehåller "id", "title", "author" (som en sträng), "isbn" och "publishedYear".
public class BookResponse {

    @Schema(description = "Title of the book")
    private String title;

    @Schema(description = "Author of the book")
    private String author;

    @Schema(description = "ISBN number")
    private String isbn;

    @Schema(description = "Year the book was published")
    private int publishedYear;

    @Schema(description = "ID of the book")
    private Long id;

    // Getters
    public String getTitle() {return title;}
    public String getAuthor() {return author;}
    public String getIsbn() {return isbn;}
    public int getPublishedYear() {return publishedYear;}
    public Long getId() {return id;}

    // Setters
    public void setTitle(String title) {this.title = title;}
    public void setAuthor(String author) {this.author = author;}
    public void setIsbn(String isbn) {this.isbn = isbn;}
    public void setPublishedYear(int publishedYear) {this.publishedYear = publishedYear;}
    public void setId(Long id) {this.id = id;}
}
