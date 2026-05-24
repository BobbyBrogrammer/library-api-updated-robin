package com.example.boilerroom;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

// Startpunkten för Library API-applikationen
// @EnableCaching aktiverar Spring Cache-stöd, exempelvis för @Cacheable i BookService
@EnableCaching
@SpringBootApplication
public class LibraryApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
