package com.example.boilerroom;

import com.example.boilerroom.dto.AuthorDTO;
import com.example.boilerroom.dto.BookRequest;
import com.example.boilerroom.dto.BookResponse;
import com.example.boilerroom.dto.LoanDTO;
import com.example.boilerroom.model.Book;
import com.example.boilerroom.repository.AuthorRepository;
import com.example.boilerroom.repository.BookRepository;
import com.example.boilerroom.repository.LoanRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryApiApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private LoanRepository loanRepository;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void postBook_shouldReturn201() {
        BookRequest request = new BookRequest();
        request.setTitle("The Hobbit");
        request.setIsbn("001");
        request.setPublishedYear(1937);

        ResponseEntity<BookResponse> response = restTemplate.postForEntity(
                "/api/v1/books", request, BookResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("The Hobbit", response.getBody().getTitle());
    }

    @Test
    void getBookById_shouldReturn200() {
        BookRequest request = new BookRequest();
        request.setTitle("Lord of the Rings");

        ResponseEntity<BookResponse> created = restTemplate.postForEntity(
                "/api/v1/books", request, BookResponse.class);

        Long id = created.getBody().getId();

        ResponseEntity<BookResponse> response = restTemplate.getForEntity(
                "/api/v1/books/" + id, BookResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Lord of the Rings", response.getBody().getTitle());
    }

    @Test
    void createAuthorAndBook_shouldReturnBookForAuthor() {
        // create author first
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setName("Tolkien");
        ResponseEntity<AuthorDTO> authorResponse = restTemplate.postForEntity(
                "/api/v1/authors", authorDTO, AuthorDTO.class);
        Long authorId = authorResponse.getBody().getId();

        // create book and link it to the author
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle("The Hobbit");
        bookRequest.setAuthorId(authorId);
        restTemplate.postForEntity("/api/v1/books", bookRequest, BookResponse.class);

        // fetch books for the author and check its there
        ResponseEntity<BookResponse[]> books = restTemplate.getForEntity(
                "/api/v1/authors/" + authorId + "/books", BookResponse[].class);

        assertEquals(HttpStatus.OK, books.getStatusCode());
        assertEquals(1, books.getBody().length);
        assertEquals("The Hobbit", books.getBody()[0].getTitle());
    }

    @Test
    void createLoan_shouldReturn201() {
        // create a book before we can loan it
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle("The Hobbit");
        ResponseEntity<BookResponse> bookResponse = restTemplate.postForEntity(
                "/api/v1/books", bookRequest, BookResponse.class);
        Long bookId = bookResponse.getBody().getId();

        // loan the book and check we get 201 back
        LoanDTO loanDTO = new LoanDTO();
        loanDTO.setBookId(bookId);
        ResponseEntity<LoanDTO> loanResponse = restTemplate.postForEntity(
                "/api/v1/loans", loanDTO, LoanDTO.class);

        assertEquals(HttpStatus.CREATED, loanResponse.getStatusCode());
        assertEquals("The Hobbit", loanResponse.getBody().getBookTitle());
    }

    @Test
    void createLoanTwice_shouldReturn400() {
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle("The Hobbit");
        ResponseEntity<BookResponse> bookResponse = restTemplate.postForEntity(
                "/api/v1/books", bookRequest, BookResponse.class);
        Long bookId = bookResponse.getBody().getId();

        // loan it once, should work fine
        LoanDTO loanDTO = new LoanDTO();
        loanDTO.setBookId(bookId);
        restTemplate.postForEntity("/api/v1/loans", loanDTO, LoanDTO.class);

        // try to loan same book again, should get 400
        ResponseEntity<String> secondLoan = restTemplate.postForEntity(
                "/api/v1/loans", loanDTO, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, secondLoan.getStatusCode());
    }

//    @Test
//    void createLoanConcurrently_onlyOneShouldSucceed() throws InterruptedException {
//        // create a book to loan
//        BookRequest bookRequest = new BookRequest();
//        bookRequest.setTitle("The Hobbit");
//        ResponseEntity<BookResponse> bookResponse = restTemplate.postForEntity(
//                "/api/v1/books", bookRequest, BookResponse.class);
//        Long bookId = bookResponse.getBody().getId();
//
//        // use CountDownLatch to fire both threads at the same time
//        CountDownLatch latch = new CountDownLatch(1);
//        List<Integer> statusCodes = Collections.synchronizedList(new ArrayList<>());
//
//        LoanDTO loanDTO = new LoanDTO();
//        loanDTO.setBookId(bookId);
//
//        Runnable task = () -> {
//            try {
//                latch.await(); // wait for the signal
//                ResponseEntity<String> response = restTemplate.postForEntity(
//                        "/api/v1/loans", loanDTO, String.class);
//                statusCodes.add(response.getStatusCode().value());
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        };
//
//        // start two threads trying to loan the same book
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//        executor.submit(task);
//        executor.submit(task);
//
//        latch.countDown(); // fire both threads at the same time
//        executor.shutdown();
//        executor.awaitTermination(5, TimeUnit.SECONDS);
//
//        // one should succeed with 201, one should fail with 400
//        assertTrue(statusCodes.contains(201));
//        assertTrue(statusCodes.contains(400));
//        assertEquals(1, loanRepository.count()); // @Transactional ensures only one loan is created
//    }

    @Test
    void createLoanConcurrently_100Threads_onlyOneShouldSucceed() throws InterruptedException {
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle("The Hobbit");
        ResponseEntity<BookResponse> bookResponse = restTemplate.postForEntity(
                "/api/v1/books", bookRequest, BookResponse.class);
        Long bookId = bookResponse.getBody().getId();

        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(1);
        List<Integer> statusCodes = Collections.synchronizedList(new ArrayList<>());

        LoanDTO loanDTO = new LoanDTO();
        loanDTO.setBookId(bookId);

        Runnable task = () -> {
            try {
                latch.await();
                ResponseEntity<String> response = restTemplate.postForEntity(
                        "/api/v1/loans", loanDTO, String.class);
                statusCodes.add(response.getStatusCode().value());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(task);
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        assertTrue(statusCodes.contains(201));
        assertEquals(1, loanRepository.count());
        System.out.println("Status codes: " + statusCodes);
    }

    @Test
    void getAuthorById_shouldReturn200() {
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setName("Tolkien");
        ResponseEntity<AuthorDTO> created = restTemplate.postForEntity(
                "/api/v1/authors", authorDTO, AuthorDTO.class);
        Long authorId = created.getBody().getId();

        ResponseEntity<AuthorDTO> response = restTemplate.getForEntity(
                "/api/v1/authors/" + authorId, AuthorDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Tolkien", response.getBody().getName());
    }

    @Test
    void getAuthorById_shouldReturn404_whenNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/authors/9999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createLoan_shouldReturn404_whenBookDoesNotExist() {
        LoanDTO loanDTO = new LoanDTO();
        loanDTO.setBookId(9999L);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/loans", loanDTO, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getLoans_shouldReturn200WithCreatedLoan() {
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle("Dune");
        ResponseEntity<BookResponse> bookResponse = restTemplate.postForEntity(
                "/api/v1/books", bookRequest, BookResponse.class);
        Long bookId = bookResponse.getBody().getId();

        LoanDTO loanDTO = new LoanDTO();
        loanDTO.setBookId(bookId);
        restTemplate.postForEntity("/api/v1/loans", loanDTO, LoanDTO.class);

        ResponseEntity<LoanDTO[]> response = restTemplate.getForEntity("/api/v1/loans", LoanDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().length);
    }

    @Test
    void getLoans_shouldReturnEmptyList_whenNoLoansExist() {
        ResponseEntity<LoanDTO[]> response = restTemplate.getForEntity("/api/v1/loans", LoanDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().length);
    }

    @Test
    void getBookById_shouldReturn404_whenNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/books/9999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllBooks_shouldReturn200() {
        BookRequest request = new BookRequest();
        request.setTitle("The Hobbit");
        restTemplate.postForEntity("/api/v1/books", request, BookResponse.class);

        ResponseEntity<BookResponse[]> response = restTemplate.getForEntity("/api/v1/books", BookResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().length);
        assertNull(response.getBody()[0].getAuthor());
    }

    @Test
    void getAllBooksV2_shouldReturn200() {
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setName("Tolkien");

        ResponseEntity<AuthorDTO> authorResponse = restTemplate.postForEntity("/api/v1/authors",
                authorDTO, AuthorDTO.class);
        Long authorId = authorResponse.getBody().getId();

        BookRequest request = new BookRequest();
        request.setTitle("The Hobbit");
        request.setAuthorId(authorId);
        restTemplate.postForEntity("/api/v1/books", request, BookResponse.class);

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v2/books", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createBook_shouldReturn404_whenAuthorDoesNotExist() {
        BookRequest request = new BookRequest();
        request.setTitle("The Hobbit");
        request.setAuthorId(9999L);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/books", request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getBookById_shouldReturn200WithNullAuthor() {
        BookRequest request = new BookRequest();
        request.setTitle("The Hobbit");

        ResponseEntity<BookResponse> created = restTemplate.postForEntity("/api/v1/books",
                request, BookResponse.class);
        Long id = created.getBody().getId();

        ResponseEntity<BookResponse> response = restTemplate.getForEntity("/api/v1/books/" + id, BookResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getAuthor());
    }

    @Test
    void getAllBooksV2_shouldReturn200WithNullAuthor() {
        BookRequest request = new BookRequest();
        request.setTitle("The Hobbit");
        restTemplate.postForEntity("/api/v1/books", request, BookResponse.class);

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v2/books", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createLoanConcurrently_demonstratesRaceCondition() throws InterruptedException {
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle("The Hobbit");
        ResponseEntity<BookResponse> bookResponse = restTemplate.postForEntity("/api/v1/books",
                bookRequest, BookResponse.class);
        Long bookId = bookResponse.getBody().getId();

        CountDownLatch latch = new CountDownLatch(1);
        List<Integer> statusCodes = Collections.synchronizedList(new ArrayList<>());

        LoanDTO loanDTO = new LoanDTO();
        loanDTO.setBookId(bookId);

        Runnable task = () -> {
            try {
                latch.await();
                ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/loans", loanDTO, String.class);
                statusCodes.add(response.getStatusCode().value());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(task);
        executor.submit(task);
        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("Status codes: " + statusCodes);
        assertEquals(2, statusCodes.size());
    }

}