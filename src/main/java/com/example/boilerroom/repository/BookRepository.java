package com.example.boilerroom.repository;
import com.example.boilerroom.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// Den här klassen hanterar databasoperationer för Book via Spring Data JPA
// findByAuthorId() används för att hämta alla böcker kopplade till en specifik författare, med pagination
public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByAuthorId(Long authorId, Pageable pageable);
}
