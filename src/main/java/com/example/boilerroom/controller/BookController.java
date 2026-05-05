package com.example.boilerroom.controller;

import com.example.boilerroom.dto.BookRequest;
import com.example.boilerroom.dto.BookResponse;
import com.example.boilerroom.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// BookControllers uppgift är att ta emot HTTP-anrop för böcker (v1) och skicka dem vidare till BookService.
// Hanterar GET /api/v1/books, GET /api/v1/books/{id} och POST /api/v1/books.
@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @Operation(summary = "Get all books")
    @ApiResponse(responseCode = "200", description = "List of all books")
    @GetMapping
    public List<BookResponse> getAll() {
        return service.getAll();
    }

    @Operation(summary = "Create a new book")
    @ApiResponse(responseCode = "201", description = "Book created successfully")
    @PostMapping
    public ResponseEntity<BookResponse> create(@RequestBody @Valid BookRequest request) {
        BookResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get a book by ID")
    @ApiResponse(responseCode = "200", description = "Book found")
    @ApiResponse(responseCode = "404", description = "Book not found")
    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }


}
