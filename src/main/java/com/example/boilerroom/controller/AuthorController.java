package com.example.boilerroom.controller;

import com.example.boilerroom.dto.AuthorDTO;
import com.example.boilerroom.dto.BookResponse;
import com.example.boilerroom.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Hanterar HTTP-anrop för författare och skickar dem vidare till AuthorService.
// POST /api/v1/authors, GET /api/v1/authors/{id} och GET /api/v1/authors/{id}/books.
@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {

    private final AuthorService service;

    public AuthorController(AuthorService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new author")
    @ApiResponse(responseCode = "201", description = "Author created successfully")
    @PostMapping
    public ResponseEntity<AuthorDTO> create(@RequestBody @Valid AuthorDTO dto) {
        AuthorDTO response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get an author by ID")
    @ApiResponse(responseCode = "200", description = "Author found")
    @ApiResponse(responseCode = "404", description = "Author not found")
    @GetMapping("/{id}")
    public AuthorDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(summary = "Get all books by author ID")
    @ApiResponse(responseCode = "200", description = "List of books for author")
    @ApiResponse(responseCode = "404", description = "Author not found")
    @GetMapping("/{id}/books")
    public List<BookResponse> getBooksByAuthorId(@PathVariable Long id) {
        return service.getBooksByAuthorId(id);
    }
}