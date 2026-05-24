package com.example.boilerroom.repository;
import com.example.boilerroom.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

// Den här klassen hanterar databasoperationer för Loan via Spring Data JPA
// existsByBookId() används för att kontrollera om en bok redan är utlånad
public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean existsByBookId(Long bookId);
}
