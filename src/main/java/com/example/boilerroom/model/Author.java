package com.example.boilerroom.model;

import jakarta.persistence.*;

import java.util.List;

// Representerar en författare i systemet.
// Innehåller ett unikt "id", "name" och en lista "books" med alla böcker kopplade till författaren.
@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "author", fetch = FetchType.EAGER)
    private List<Book> books;

    public Author() {}

    // Getters
    public Long getId() {return id;}
    public String getName() {return name;}
    public List<Book> getBooks() {return books;}

    // Setters
    public void setId(Long id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setBooks(List<Book> books) {this.books = books;}
}
