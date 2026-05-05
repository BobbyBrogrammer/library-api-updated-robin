package com.example.boilerroom.controller;

import com.example.boilerroom.dto.BookListResponseV2;
import com.example.boilerroom.dto.BookResponseV2;
import com.example.boilerroom.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Hanterar GET /api/v2/books.
// Returnerar böcker med fältet "available" wrappat i ett svar med "data" och "version".
@RestController
    @RequestMapping("/api/v2/books")
    public class BookControllerV2 {
        private final BookService service;

        public BookControllerV2(BookService service) {this.service = service;}

        @Operation(summary = "Get all books")
        @ApiResponse(responseCode = "200", description = "List of all books")
        @GetMapping
        public BookListResponseV2 getAll() {
            List<BookResponseV2> books = service.getAllV2();
            BookListResponseV2 response = new BookListResponseV2();
            response.setVersion("v2");
            response.setData(books);
            return response;
        }


    }

