# Library API

Jag har byggt ett REST API för ett bibliotekssystem byggt med Spring Boot 3.5 och Java 21.
Man kan hantera böcker, författare och lån via HTTP-anrop, ungefär som ett riktigt
bibliotekssystem fast i liten skala.

All data sparas i en H2 in-memory databas så allt försvinner när man stänger ner appen.
Det är inte byggt för produktion, fokus ligger på att lära sig hur Spring Boot fungerar,
inte att driftsätta något.

---

## Hur man kör projektet

**Krav:** Java 21, Maven

```bash
git clone https://github.com/BobbyBrogrammer/library-api-updated-robin.git
cd library-api-updated-robin
mvn spring-boot:run
```

Appen startar på `http://localhost:8080`

**Kör testerna:**
```bash
mvn test
```

---

## Autentisering

Alla endpoints kräver HTTP Basic Auth.

| Användarnamn | Lösenord |
|---|---|
| robin | secret |

Exempel med curl:
```bash
curl -u robin:secret http://localhost:8080/api/v1/books
```

---

## API-endpoints

### Böcker
| Metod | Endpoint | Beskrivning |
|---|---|---|
| GET | `/api/v1/books` | Hämta alla böcker (paginerat) |
| GET | `/api/v1/books/{id}` | Hämta en specifik bok |
| POST | `/api/v1/books` | Skapa en ny bok |
| GET | `/api/v1/books/{id}/external-info` | Hämta extern bokinformation via ISBN (Open Library) |
| GET | `/api/v2/books` | Hämta alla böcker med tillgänglighetsstatus (paginerat) |

### Författare
| Metod | Endpoint | Beskrivning |
|---|---|---|
| POST | `/api/v1/authors` | Skapa en ny författare |
| GET | `/api/v1/authors/{id}` | Hämta en specifik författare |
| GET | `/api/v1/authors/{id}/books` | Hämta alla böcker för en författare (paginerat) |

### Lån
| Metod | Endpoint | Beskrivning |
|---|---|---|
| POST | `/api/v1/loans` | Skapa ett nytt lån |
| GET | `/api/v1/loans` | Hämta alla lån (paginerat) |

---

## Swagger UI

När appen körs kan man gå till `http://localhost:8080/swagger-ui.html` och se
alla endpoints och testa dem direkt i webbläsaren utan att behöva Postman eller liknande.

---

## Hur är koden strukturerad?

Jag har delat upp koden i lager. Varje lager har ett eget ansvar och pratar
bara med lagret direkt bredvid sig:

```
Controller  →  Service  →  Repository  →  Databas
```

Det gör det mycket lättare att hitta i koden. Går något fel vet jag direkt
var jag ska leta. Är det ett databasfel? `Repository`. Är det logiken som strular?
`Service`. Returnerar API:et fel data? `Controller`.

---

## Modellerna

Modellerna representerar de verkliga objekten i systemet och mappas direkt mot
tabeller i databasen via JPA och Hibernate. Jag behöver alltså inte skriva SQL
för att skapa tabellerna, det sköts automatiskt.

`Author` representerar en författare med id, namn och en lista med böcker.
Relationen till `Book` är `@OneToMany`, en författare kan ha många böcker.
Jag använder `FetchType.LAZY` vilket betyder att böckerna inte laddas automatiskt
när man hämtar en författare, utan bara när de faktiskt behövs. Det undviker
onödig databaselastning.

`Book` representerar en bok med id, titel, isbn, utgivningsår och en koppling
till en `Author` via `@ManyToOne`. Boken har också ett version-fält med `@Version`
för optimistic locking, mer om det längre ner.

`Loan` representerar ett lån med id, koppling till en bok, lånedatum och
returdatum. Returdatum är `null` tills boken faktiskt lämnas tillbaka.
Det viktiga är att jag satt `unique = true` på `book_id` kolumnen. Det gör det
omöjligt för databasen att lagra två aktiva lån för samma bok.

---

## Repositories

Repositories är gränssnittet mot databasen. Jag använder Spring Data JPA
och det är lite magiskt, genom att ärva från `JpaRepository<T, Long>` får
jag `save()`, `findById()`, `findAll()`, `deleteAll()` och fler gratis. Spring
genererar SQL-frågorna automatiskt i bakgrunden.

Egna metoder som lagts till:

```java
// LoanRepository
boolean existsByBookId(Long bookId);

// BookRepository
Page<Book> findByAuthorId(Long authorId, Pageable pageable);
```

Spring förstår vad metoderna ska göra bara utifrån vad de heter. Ingen SQL behövs, vilket jag 
tycker är riktigt nice.

---

## DTOs

DTO står för Data Transfer Object.

Om man skickar ut modellerna direkt riskerar man att exponera data man inte vill
att användaren ska se. Man låser också API:et hårt mot hur databasen ser ut.
Med DTO:s kan jag forma svaret precis som jag vill, helt oberoende av databasen.

| DTO | Syfte |
|---|---|
| `AuthorDTO` | Används både som request och response för författare |
| `BookRequest` | Inkommande data när en bok skapas |
| `BookResponse` | Utdata för en bok i v1, utan lånestatus |
| `BookResponseV2` | Utdata för en bok i v2, med ett `available`-fält |
| `BookListResponseV2` | Wrappat v2-svar med `version` och `data` |
| `LoanDTO` | Request innehåller bara `bookId`, response innehåller boktitel och datum |
| `ErrorResponse` | Strukturerat felsvar med timestamp, statuskod, meddelande och path |

---

## Services

Services är hjärtat i applikationen, det är här affärslogiken bor.
Controllern tar emot anropet men det är alltid servicen som bestämmer vad som händer.

`AuthorService` skapar författare, hämtar en specifik författare och hämtar alla
böcker för en given författare via en paginerad databasfråga.

`BookService` hämtar och skapar böcker och kan koppla en bok till en författare
om man anger ett `authorId`. I v2-varianten `getAllV2()` kollar den också om varje
bok är utlånad just nu via `LoanRepository`, det är därifrån `available`-fältet kommer.
`getById()` är märkt med `@Cacheable` vilket gör att resultatet cachas efter första
anropet.

`LoanService` – när man försöker låna en bok händer detta:
1. Kontrollera att boken faktiskt finns, annars kastas `BookNotFoundException`
2. Kontrollera att boken inte redan är utlånad via `existsByBookId()`, annars kastas `BookAlreadyOnLoanException`
3. Om allt är okej skapas lånet och sparas

Metoden är märkt med `@Transactional`, mer om det längre ner.

`ExternalBookService` hämtar bokinformation från Open Library API via ISBN.
Den är skyddad med en `@CircuitBreaker` (Resilience4j), mer om det längre ner.

---

## Controllers

Controllers är det yttersta lagret, det är hit HTTP-anropen kommer in.
De tar emot anropet, validerar att datan är rätt och skickar vidare
till rätt service. Ingen logik alls, bara vidarebefordran.

`AuthorController` hanterar `/api/v1/authors`: `POST` skapar en ny författare,
`GET /{id}` hämtar en specifik författare, `GET /{id}/books` hämtar alla böcker för
en författare.

`BookController` hanterar `/api/v1/books`: `POST` skapar en ny bok,
`GET` hämtar alla böcker, `GET /{id}` hämtar en specifik bok,
`GET /{id}/external-info` hämtar extern bokinformation från Open Library.

`BookControllerV2` hanterar `/api/v2/books`: `GET` hämtar alla böcker med ett
`available`-fält som visar om boken är ledig. Svaret är wrappat i ett objekt med
`version` och `data` så man direkt ser att man pratar med v2 av API:et.

`LoanController` hanterar `/api/v1/loans`: `POST` skapar ett nytt lån,
`GET` hämtar alla lån.

---

## Säkerhet

Spring Security är konfigurerat med HTTP Basic Auth och stateless sessionshantering
(ingen session sparas på servern). Varje anrop måste skicka med credentials i headern.

Användarnamn och lösenord hämtas från `application.properties` (eller Vault i ett
produktionsscenario). Lösenordet lagras aldrig i klartext, det krypteras med BCrypt
innan det jämförs.

CORS är konfigurerat för att tillåta anrop från `http://localhost:3000` vilket gör
att en frontend (t.ex. React) kan prata med API:et lokalt.

---

## Rate Limiting

Bucket4j används för att begränsa hur många anrop en enskild IP-adress får göra.
Gränsen är 100 anrop per minut. Går man över det returneras `429 Too Many Requests`.

Varje IP-adress får en egen bucket som fylls på automatiskt efter en minut.
Filtret är implementerat som en `OncePerRequestFilter` vilket gör att det körs
en gång per HTTP-anrop innan det når controllern.

I testerna inaktiveras rate limitern via `app.ratelimit.enabled=false` för att
testerna inte ska blockera varandra.

---

## Caching

`@EnableCaching` aktiveras i huvudklassen och Spring Cache används för att cacha
resultatet av `BookService.getById()`. Det betyder att första gången man hämtar
en bok görs ett databasanrop, men efterföljande anrop med samma id hämtas direkt
från cachen.

I produktion är Redis konfigurerat som cache-backend. I testerna inaktiveras
cachen via `spring.cache.type=none`.

---

## Vault

Spring Cloud Vault är konfigurerat för att hämta känslig konfiguration (t.ex.
lösenord) från HashiCorp Vault i produktion. I testerna och lokal utveckling
är Vault valfritt via `spring.config.import=optional:vault://` och konfigurationen
hämtas direkt från `application.properties` istället.

---

## Externt API och Circuit Breaker

`ExternalBookService` anropar Open Library API (`https://openlibrary.org/isbn/`)
för att hämta extra information om en bok baserat på ISBN.

Eftersom externa API:er kan vara opålitliga är anropet skyddat med en Circuit Breaker
(Resilience4j). Om API:et inte svarar eller returnerar fel öppnas kretsen och
ett fallback-svar returneras istället:

```json
{
  "message": "External book info is temporarily unavailable",
  "isbn": "..."
}
```

Det gör att resten av applikationen fortsätter fungera även om Open Library är nere.

---

## Felhantering

Felhanteringen är centraliserad i `GlobalExceptionHandler` med `@RestControllerAdvice`.
Spring skickar automatiskt alla undantag dit, vilket gör att jag bara behöver
skriva felhanteringskoden en gång för hela applikationen.

Flödet:
```
Anrop kommer in → Controller → Service kastar undantag
  → GlobalExceptionHandler fångar det → strukturerat felsvar skickas tillbaka
```

| Undantag | HTTP-status | När |
|---|---|---|
| `BookNotFoundException` | 404 | Bok inte hittad |
| `AuthorNotFoundException` | 404 | Författare inte hittad |
| `BookAlreadyOnLoanException` | 400 | Boken är redan utlånad |
| `MethodArgumentNotValidException` | 400 | Valideringen misslyckas (t.ex. tom titel) |
| `DataIntegrityViolationException` | 400 | Databasens unique constraint bryts (extra skyddsnät vid race conditions) |

---

## @Transactional

`@Transactional` på `LoanService.create()` gör att hela metoden körs som en enda
enhet – antingen går allting igenom, eller så rullas allt tillbaka. Det kallas atomicitet.

Problemet det ska lösa är race conditions. Tänk om två användare försöker låna
samma bok exakt samtidigt:

1. Tråd A kollar – boken är ledig!
2. Tråd B kollar – boken är ledig!
3. Båda försöker skapa ett lån.

`@Transactional` hjälper till att hålla ihop operationerna, men det är `unique` constrainten 
på `book_id` som är det verkliga sista skyddsnätet. Databasen vägrar
att spara ett andra lån för samma bok, och `DataIntegrityViolationException` fångas
upp av `GlobalExceptionHandler` och returneras som `400`.

---

## @Version och optimistic locking

`@Version` på `Book` ger varje rad i databasen ett versionsnummer. När man läser
en bok får man med versionsnumret. När man sparar kontrollerar databasen att
versionsnumret inte ändrats sedan man läste. Har någon annan hunnit spara emellan
får man ett undantag istället för att tyst skriva över deras data.

Det kallas optimistic locking för att man optimistiskt antar att konflikter
är sällsynta och bara kontrollerar vid sparning. Det är grunden för framtida
uppdateringsendpoints.

---

## Testerna

Alla tester är integrationstester vilket betyder att hela applikationen faktiskt
startar upp på en slumpmässig port och testerna skickar riktiga HTTP-anrop med
`TestRestTemplate`. Ingen mockning – det beter sig exakt som en riktig användare.

`@BeforeEach` rensar hela databasen innan varje test så att de inte påverkar varandra.
Alla anrop görs via en `auth()`-hjälpmetod som automatiskt lägger till Basic Auth-credentials.

Testkonfigurationen (`src/test/resources/application.properties`) gör följande:
- Vault görs valfritt (`optional:vault://`)
- Redis-cache inaktiveras (`spring.cache.type=none`)
- `data.sql` körs inte (`spring.sql.init.mode=never`)
- Spring Cloud kompatibilitetskontrollen inaktiveras
- Rate limitern inaktiveras (`app.ratelimit.enabled=false`)

**Tester värda att nämna:**

`createLoanTwice_shouldReturn400` – lånar samma bok två gånger och kontrollerar
att det andra försöket ger `400`. Enkelt men viktigt.

`createLoanConcurrently_100Threads_onlyOneShouldSucceed` – 100 trådar försöker
låna samma bok exakt samtidigt med hjälp av en `CountDownLatch` som håller alla
trådar redo och släpper dem på en gång. Sedan kontrolleras att exakt ett lån skapades.
Det bevisar att concurrency-skyddet håller under hög belastning.

`createLoanConcurrently_demonstratesRaceCondition` – samma idé fast med bara
2 trådar. Verifierar att systemet alltid hanterar situationen och ger tillbaka
2 svar oavsett utfall.

---

## Sammanfattning

| Lager | Klasser | Ansvar |
|---|---|---|
| Model | `Author`, `Book`, `Loan` | Representerar data och mappas mot databasen |
| Repository | `AuthorRepository`, `BookRepository`, `LoanRepository` | Pratar med databasen |
| Service | `AuthorService`, `BookService`, `LoanService`, `ExternalBookService` | All affärslogik |
| Controller | `AuthorController`, `BookController`, `BookControllerV2`, `LoanController` | Tar emot HTTP-anrop |
| DTO | `AuthorDTO`, `BookRequest`, `BookResponse`, `BookResponseV2`, `LoanDTO` m.fl. | Filtrerar data in och ut |
| Exception | `BookNotFoundException`, `AuthorNotFoundException`, `BookAlreadyOnLoanException`, `GlobalExceptionHandler` | Felhantering |
| Config | `SecurityConfig`, `AppConfig`, `RateLimitFilter` | Säkerhet, konfiguration och rate limiting |
