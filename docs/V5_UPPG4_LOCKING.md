# V5 Uppgift 4 - Optimistic Locking med @Version

---
### Vad är optimistic locking?
- Optimistic Locking - är ett sätt att skydda data när flera trådar försöker ändra 
samma sak samtidigt. 
- Istället för att låsa raden direkt så antar man att 
konflikter är sällsynta och kontrollerar istället när man ska spara.

### Så hur funkar optimistic locking egentligen?
- Varje rad i databasen får ett versionsnummer
- När du läser en rad får du med versionsnumret
- När du sparar kollar databasen att versionsnumret inte ändrats
- Om någon annan hann spara först = krasch, du får ett undantag

---

### Vad är pessimistic locking?
Pessimistic locking låser raden i databasen direkt när du läser den.
Ingen annan tråd kan läsa eller skriva till raden förrän låset släpps.
Det används när konflikter är vanliga och man inte har råd att misslyckas.

### Optimistic vs Pessimistic – när ska man använda vad?
- Optimistic: när konflikter är sällsynta, t.ex. uppdatering av bokinformation
- Pessimistic: när konflikter är vanliga och data är kritisk, t.ex. banktransaktioner

Optimistic är lättare och snabbare vid låg konflikt.
Pessimistic är säkrare men skapar köer och kan bli en flaskhals.

---

## Varför Book och inte Loan?

**Book-** är den resursen som flera användare kan försöka ändra samtidigt.
**Loan-** har redan unique constraint på book_id som skyddar mot dubbla lån.

- Men! Book kan behöva skyddas i framtiden mot att två användare uppdaterar samma bok 
samtidigt.

*Just nu finns det ingen endpoint för att uppdatera en bok, men om man lägger 
till det i framtiden så är @Version redan på plats.*

**Utan @Version hade data kunnat skrivas över helt tyst, ingen error, ingen 
varning, bara fel data i databasen.**

---

### Hur jag implementerade det:
Jag lade till **@Version** på Book-entiteten *samt getters och setters för fältet:*
```java
@Version
private Long version;

public Long getVersion() { return version; }
public void setVersion(Long version) {this.version = version; }
```

*Det är allt som behövdes!*

**JPA och Hibernate sköter sen resten automatiskt. Varje gång en bok uppdateras ökar
versionen med `1`.**

---