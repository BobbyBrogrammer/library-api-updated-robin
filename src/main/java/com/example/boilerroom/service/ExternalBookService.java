package com.example.boilerroom.service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;

// Den här klassen ansvarar för att hämta bokinformation från Open Library API via ISBN
// @CircuitBreaker säkerställer att ett fallback-svar returneras om det externa API:et inte svarar
@Service
public class ExternalBookService {
    private static final Logger log = LoggerFactory.getLogger(ExternalBookService.class);
    private final RestTemplate restTemplate;

    @Value("${external.api.openlibrary.url}")
    private String openLibraryUrl;

    public ExternalBookService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "openLibrary", fallbackMethod = "fallback")
    public Map<String, Object> getExternalBookInfo(String isbn) {
        String url = openLibraryUrl + isbn + ".json";
        return restTemplate.getForObject(url, Map.class);
    }

    public Map<String, Object> fallback(String isbn, Throwable t) {
        log.warn("Circuit breaker got triggered for ISBN {}: {}", isbn, t.getMessage());
        return Map.of("message", "External book info is temporarily unavailable", "isbn", isbn);
    }
}
