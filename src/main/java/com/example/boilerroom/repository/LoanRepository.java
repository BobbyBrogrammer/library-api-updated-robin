package com.example.boilerroom.repository;

import com.example.boilerroom.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean existsByBookId(Long bookId);
}
