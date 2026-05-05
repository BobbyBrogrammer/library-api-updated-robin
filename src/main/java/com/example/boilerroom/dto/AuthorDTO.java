package com.example.boilerroom.dto;

import jakarta.validation.constraints.NotBlank;

// AuthorDTOs uppgift är att representera en författare i både request och response.
// Innehåller "id", "name" (obligatorisk) och "bookCount" som visar hur många böcker författaren har.
public class AuthorDTO {
    private Long id;
    @NotBlank(message = "Author name is required")
    private String name;
    private int bookCount;

    // Getters
    public Long getId() {return id;}
    public String getName() {return name;}
    public int getBookCount() {return bookCount;}

    // Setters
    public void setId(Long id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setBookCount(int bookCount) {this.bookCount = bookCount;}
}
