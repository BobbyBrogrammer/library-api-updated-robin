# V5 Uppgift 3 - Begränsningar med @Transactional

---

### @Transactional är rätt så nice men det löser inte allt.

Det som @Transactional faktiskt gör är att den ser till att hela metoden körs 
som en enhet, det kallas atomicitet.
Antingen går allt igenom, eller så rullas allt tillbaka.

### Det låter ju kanon!?

*Men problemet är att det fortfarande kan gå fel om två anrop kommer in exakt
samtidigt.* 

@Transactional skyddar inte mot att två trådar läser samma data 
innan någon av dem hinner skriva. Den ser bara till att det som händer inuti 
metoden är konsekvent, men den stoppar inte en annan tråd från att smita in 
mellan läsningen och skrivningen.

---

### Tänk såhär:
- Två trådar läser att boken är ledig samtidigt, båda ser att 
existsByBookId() returnerar false, och båda försöker skapa ett lån.
- @Transactional hjälper inte här för att båda redan hann läsa innan någon
hann skriva.

---

**I mitt projekt så fångade H2:s unique constraint upp det här istället för min
egen kod, vilket tekniskt fungerade men det är inte meningen att databasen ska 
vara den som räddar oss.**

### Det är vår affärslogik som ska hålla koll!

- **Det är därför man behöver locking ovanpå @Transactional.**

---