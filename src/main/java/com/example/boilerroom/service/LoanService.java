package com.example.boilerroom.service;

import com.example.boilerroom.dto.LoanDTO;
import com.example.boilerroom.exception.BookAlreadyOnLoanException;
import com.example.boilerroom.exception.BookNotFoundException;
import com.example.boilerroom.model.Book;
import com.example.boilerroom.model.Loan;
import com.example.boilerroom.repository.BookRepository;
import com.example.boilerroom.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// LoanServices uppgift är att hantera logiken för lån och hämta data från LoanRepository och BookRepository.
// Affärsregeln att en bok bara får ha ett aktivt lån upprätthålls här.
// @Transactional på create() används för att förhindra race conditions vid samtidiga anrop.
@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public LoanDTO create(Long bookId) {
        Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

        if (loanRepository.existsByBookId(bookId)) {
            throw new BookAlreadyOnLoanException(bookId);
        }

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now());
        loan.setReturnDate(null);

        Loan saved = loanRepository.save(loan);

        LoanDTO response = new LoanDTO();
        response.setId(saved.getId());
        response.setBookId(saved.getBook().getId());
        response.setBookTitle(saved.getBook().getTitle());
        response.setLoanDate(saved.getLoanDate());
        response.setReturnDate(saved.getReturnDate());
        return response;
    }

    public List<LoanDTO> getAll() {
        return loanRepository.findAll().stream()
                .map(loan -> {
                    LoanDTO dto = new LoanDTO();
                    dto.setId(loan.getId());
                    dto.setBookId(loan.getBook().getId());
                    dto.setBookTitle(loan.getBook().getTitle());
                    dto.setLoanDate(loan.getLoanDate());
                    dto.setReturnDate(loan.getReturnDate());
                    return dto;
                })
                .toList();
    }
}
