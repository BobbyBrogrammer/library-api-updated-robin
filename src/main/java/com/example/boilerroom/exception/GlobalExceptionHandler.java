package com.example.boilerroom.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import com.example.boilerroom.dto.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

// Fångar upp undantag och returnerar strukturerade felsvar med rätt HTTP-statuskod.
// Hanterar 404 (ej hittad), 400 (redan utlånad, valideringsfel och DB-konflikt).
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(LocalDateTime.now().toString(), 404, "Not found",
                ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        ErrorResponse error = new ErrorResponse(LocalDateTime.now().toString(), 400, "Bad Request",
                message, request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BookAlreadyOnLoanException.class)
    public ResponseEntity<ErrorResponse> handleBookAlreadyOnLoan(
            BookAlreadyOnLoanException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(LocalDateTime.now().toString(), 400, "Bad Request",
                ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now().toString(),
                400,
                "Bad Request",
                "Book is already on loan",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}

