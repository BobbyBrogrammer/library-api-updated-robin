package com.example.boilerroom.model;
import jakarta.persistence.*;

// Books uppgift är att representera en bok i systemet.
// Innehåller ett unikt "id", "title", "isbn" och "publishedYear", samt en koppling till en författare via "author".
// "version" används för optimistic locking – skyddar mot att två trådar skriver över varandra samtidigt.
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String title;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;
    private String isbn;
    private int publishedYear;

    public Book(){}
    public Book(String title, Author author, String isbn, int publishedYear) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publishedYear = publishedYear;
    }
    // Getters
    public Long getId() {return id;}
    public Long getVersion() {return version;}
    public String getTitle() {return title;}
    public Author getAuthor() {return author;}
    public String getIsbn() {return isbn;}
    public int getPublishedYear() {return publishedYear;}

    // Setters
    public void setId(Long id) {this.id = id;}
    public void setVersion(Long version) {this.version = version;}
    public void setTitle(String title) {this.title = title;}
    public void setAuthor(Author author) {this.author = author;}
    public void setIsbn(String isbn) {this.isbn = isbn;}
    public void setPublishedYear(int publishedYear) {this.publishedYear = publishedYear;}
}