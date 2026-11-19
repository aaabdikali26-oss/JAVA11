package kz.kinopark.service;

import kz.kinopark.exception.SeatUnavailableException;
import kz.kinopark.model.*;
import kz.kinopark.storage.CsvStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Бірыңғай билет сату жүйесі: онлайн да, касса да осы бір сервис арқылы өтеді,
 * сондықтан бір орынға екі рет билет сатылмайды.
 */
public class BookingService {
    public static final int HOLD_MINUTES = 5;

    private final CsvStorage storage;
    private final Map<Integer, Hall> halls = new LinkedHashMap<>();
    private final List<Movie> movies = new ArrayList<>();
    private final Map<Integer, Session> sessions = new LinkedHashMap<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private int userCounter = 0;

    public BookingService(CsvStorage storage) {
        this.storage = storage;
        halls.put(1, new Hall(1, "Зал 1", 6, 8));
        halls.put(2, new Hall(2, "Зал 2 (VIP)", 4, 6));
        load();
    }

    // ---------- жүктеу / сақтау ----------
    private void load() {
        movies.addAll(storage.loadMovies());
        if (movies.isEmpty()) {
            seed();
            return;
        }
        Map<Integer, Movie> movieMap = new HashMap<>();
        for (Movie m : movies) {
            movieMap.put(m.getMovieId(), m);
        }
        for (Session s : storage.loadSessions(movieMap, halls)) {
            sessions.put(s.getSessionId(), s);
        }
        for (Ticket t : storage.loadTickets(sessions)) {
            tickets.add(t);
            if (t.isActive()) {
                t.getSession().markSold(t.getRow(), t.getNumber());
            }
        }
    }

    private void seed() {
        Movie m1 = new Movie(1, "Интерстеллар", "Фантастика", 169);
        Movie m2 = new Movie(2, "Қоңыр аю", "Комедия", 95);
        Movie m3 = new Movie(3, "Тау арасындағы із", "Триллер", 112);
        movies.addAll(List.of(m1, m2, m3));
        LocalDate d = LocalDate.now().plusDays(1);
        addSessionInternal(new Session(1, m1, halls.get(1), LocalDateTime.of(d, java.time.LocalTime.of(18, 30)), 2500));
        addSessionInternal(new Session(2, m1, halls.get(2), LocalDateTime.of(d, java.time.LocalTime.of(21, 0)), 4000));
        addSessionInternal(new Session(3, m2, halls.get(1), LocalDateTime.of(d, java.time.LocalTime.of(15, 0)), 2000));
        addSessionInternal(new Session(4, m3, halls.get(2), LocalDateTime.of(d, java.time.LocalTime.of(20, 0)), 3500));
        saveAll();
    }

    private void addSessionInternal(Session s) {
        sessions.put(s.getSessionId(), s);
    }

    private void saveAll() {
        storage.saveMovies(movies);
        storage.saveSessions(sessions.values());
        storage.saveTickets(tickets);
    }

    // ---------- анықтамалық ----------
    public int nextUserId() {
        return ++userCounter;
    }

    public List<Movie> getMovies() { return Collections.unmodifiableList(movies); }
    public Collection<Session> getSessions() { return Collections.unmodifiableCollection(sessions.values()); }
    public Collection<Hall> getHalls() { return Collections.unmodifiableCollection(halls.values()); }
    public List<Ticket> getTickets() { return Collections.unmodifiableList(tickets); }
    public Session findSession(int id) { return sessions.get(id); }
    public Hall findHall(int id) { return halls.get(id); }

    public Movie findMovie(int id) {
        for (Movie m : movies) {
            if (m.getMovieId() == id) {
                return m;
            }
        }
        return null;
    }

    // ---------- әкімші ----------
    public synchronized Movie addMovie(String title, String genre, int duration) {
        int id = movies.stream().mapToInt(Movie::getMovieId).max().orElse(0) + 1;
        Movie m = new Movie(id, title, genre, duration);
        movies.add(m);
        saveAll();
        return m;
    }

    public synchronized Session addSession(Movie movie, Hall hall, LocalDateTime time, double price) {
        int id = sessions.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        Session s = new Session(id, movie, hall, time, price);
        sessions.put(id, s);
        saveAll();
        return s;
    }

    // ---------- сату ----------
    /** Клиент орынды уақытша брондайды. */
    public void reserve(Session session, int row, int number, User user) throws SeatUnavailableException {
        session.reserveSeat(row, number, user.getHolderId(), HOLD_MINUTES);
    }

    public boolean cancelReservation(Session session, int row, int number, User user) {
        return session.releaseIfHeldBy(row, number, user.getHolderId());
    }

    /** Онлайн сату (клиент) және касса арқылы сату — бір әдіс, бір ереже. */
    public synchronized Ticket sell(Session session, int row, int number, User seller, String buyerName,
                                    Discount discount, String payMethod, String channel)
            throws SeatUnavailableException {
        session.sellSeat(row, number, seller.getHolderId());       // орын бос емес болса — exception
        double finalPrice = discount.calculateDiscount(session.getPrice());
        Payment payment = new Payment(tickets.size() + 1, finalPrice, payMethod);
        if (!payment.processPayment()) {                           // төлем өтпесе — ROLLBACK
            session.releaseSeat(row, number);
            throw new SeatUnavailableException("Төлем өтпеді, орын босатылды.");
        }
        Ticket t = new Ticket(Ticket.generateTicketCode(tickets.size() + 1), session, row, number,
                finalPrice, channel, buyerName, payment, Ticket.ACTIVE);
        tickets.add(t);
        storage.saveTickets(tickets);                              // COMMIT
        return t;
    }

    public synchronized boolean cancelTicket(String code) {
        for (Ticket t : tickets) {
            if (t.getTicketCode().equalsIgnoreCase(code) && t.isActive()) {
                t.cancelTicket();
                t.getSession().releaseSeat(t.getRow(), t.getNumber());
                storage.saveTickets(tickets);
                return true;
            }
        }
        return false;
    }

    public List<Ticket> ticketsOf(String customerName) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t.getCustomerName().equalsIgnoreCase(customerName)) {
                result.add(t);
            }
        }
        return result;
    }
}
