package com.example.boilerroom.repository;
import com.example.boilerroom.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

// Den här klassen hanterar databasoperationer för Author via Spring Data JPA
// Den ärver standardmetoder som save(), findById() och findAll() från JpaRepository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
