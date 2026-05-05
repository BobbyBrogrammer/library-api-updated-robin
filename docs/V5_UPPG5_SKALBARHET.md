# V5 Uppgift 5 - Skalbarhet

---

### Vad händer när systemet växer?
När fler användare börjar använda systemet samtidigt så ökar trycket på backend.
Spring Boot hanterar varje HTTP-anrop i en egen tråd, men det finns en gräns för 
hur många trådar som kan köras samtidigt.

### Så var blir flaskhalsen?

I mitt LibraryAPI är det databasen som är den svaga punkten.
Varje anrop gör minst ett databasanrop, och ju fler användare desto fler anrop
köar upp mot databasen.
- Spring Boot använder HikariCP som connection pool
- Standard är 10 anslutningar mot databasen
- Vid 1000 samtidiga anrop väntar 990 på en ledig anslutning

> **Connection** pool-konfiguration är inte satt i `application.properties` eftersom
> projektet använder H2 in-memory och inte en produktionsdatabas. I ett riktigt system 
> med PostgreSQL hade `maximum-pool-size` satts explicit baserat på antal CPU-kärnor 
> och förväntad belastning — exempelvis `(antal kärnor × 2) + 1` som tumregel.
---

### H2 vs Produktion
H2 är en in-memory databas som är perfekt för utveckling och testning, men den 
är inte byggd för hög belastning. I ett riktigt system hade man använt 
PostgreSQL eller liknande.

---

## När behövs caching?
*Om samma data hämtas ofta och ändras sällan*

**T.ex:** `GET /api/v1/books`, så kan man cacha svaret i minnet istället för 
att fråga databasen varje gång.

- **Det avlastar databasen kraftigt vid hög belastning.**

---

## När behövs köer?
Om en operation tar lång tid, till exempel att skicka email eller generera
en rapport, vill man inte att användaren ska behöva vänta på svar. Det skulle kännas
som om appen hänger sig, och vid hög belastning skulle det snabbt bli en flaskhals.

**Då lägger man jobbet i en kö istället, användaren får svar direkt och jobbet sköts i
bakgrunden.**
- Användaren slipper vänta, får svar direkt
- Jobbet körs i bakgrunden av en separat process som plockar upp jobb från kön och
hanterar dem en efter en.
- Systemet håller sig responsivt även vid hög belastning

Exempel: RabbitMQ, Kafka

---
