# Kinopark — билет сату жүйесі (Java 17, консоль)

## VS Code-та іске қосу
1. JDK 17+ және "Extension Pack for Java" орнатыңыз.
2. `kinopark` папкасын VS Code-та ашыңыз (File → Open Folder).
3. `src/main/java/kz/kinopark/Main.java` ашып, **Run** батырмасын басыңыз
   (немесе Run and Debug → "Kinopark").
4. Maven бар болса: `mvn test` (тесттер), `mvn exec:java` (іске қосу).

Деректер `data/` папкасында CSV болып сақталады (movies, sessions, tickets).
Тазадан бастау үшін `data/` папкасын өшіріңіз.

## Талаптар қайда орындалған
- Инкапсуляция: private өрістер + getter (Seat, Session, Ticket...)
- Мұрагерлік: User → Customer / Cashier / Admin
- Абстракция: `abstract User`, `interface Discount`
- Полиморфизм: `getRole()`, `Discount.calculateDiscount()` (StudentDiscount / NoDiscount)
- Коллекциялар: ArrayList (movies, tickets), HashMap/LinkedHashMap (sessions, seats)
- Өз exception: `SeatUnavailableException`
- Файл: `CsvStorage`
- Негізгі ереже: `Session.sellSeat/reserveSeat` (synchronized) — бір орынға бір билет
