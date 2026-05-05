package com.example.boilerroom.repository;

import com.example.boilerroom.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
