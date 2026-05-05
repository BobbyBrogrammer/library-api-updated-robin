package com.example.boilerroom.controller;

import com.example.boilerroom.dto.LoanDTO;
import com.example.boilerroom.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Tar emot HTTP-anrop för lån och skickar dem vidare till LoanService.
// Hanterar POST /api/v1/loans och GET /api/v1/loans.
@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService service;

    public LoanController(LoanService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new loan")
    @ApiResponse(responseCode = "201", description = "Loan created successfully")
    @ApiResponse(responseCode = "400", description = "Book is already on loan")
    @ApiResponse(responseCode = "404", description = "Book not found")
    @PostMapping
    public ResponseEntity<LoanDTO> create(@RequestBody LoanDTO dto) {
        LoanDTO response = service.create(dto.getBookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all loans")
    @ApiResponse(responseCode = "200", description = "List of all loans")
    @GetMapping
    public List<LoanDTO> getAll() {
        return service.getAll();
    }
}