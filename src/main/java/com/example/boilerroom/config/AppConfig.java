package com.example.boilerroom.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Konfigurationsklass som registrerar en RestTemplate-bean
// RestTemplate används av ExternalBookService för att anropa Open Library API
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
