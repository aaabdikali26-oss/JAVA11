package kz.kinopark.model;

import kz.kinopark.exception.SeatUnavailableException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Сеанс. Әр сеанстың өз орындары бар (HashMap).
 * Барлық өзгерістер synchronized: онлайн және касса бір уақытта
 * бір орынды ала алмайды.
 */
public class Session {
    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final int sessionId;
    private final Movie movie;
    private final Hall hall;
    private final LocalDateTime dateTime;
    private final double price;
    private final Map<String, Seat> seats = new LinkedHashMap<>();

    public Session(int sessionId, Movie movie, Hall hall, LocalDateTime dateTime, double price) {
        this.sessionId = sessionId;
        this.movie = movie;
        this.hall = hall;
        this.dateTime = dateTime;
        this.price = price;
        for (Seat s : hall.createSeats()) {
            seats.put(s.getKey(), s);
        }
    }

    private Seat findSeat(int row, int number) throws SeatUnavailableException {
        Seat seat = seats.get(row + "-" + number);
        if (seat == null) {
            throw new SeatUnavailableException("Мұндай орын залда жоқ: қатар " + row + ", орын " + number);
        }
        return seat;
    }

    public synchronized boolean isSeatAvailable(int row, int number) {
        Seat s = seats.get(row + "-" + number);
        return s != null && s.getStatus() == SeatStatus.AVAILABLE;
    }

    /** Орынды уақытша брондау. */
    public synchronized void reserveSeat(int row, int number, String holder, int minutes)
            throws SeatUnavailableException {
        Seat seat = findSeat(row, number);
        checkFreeFor(seat, holder);
        seat.reserve(holder, minutes);
    }

    /** Орынды сату: бос болса немесе осы адам брондаған болса ғана. */
    public synchronized void sellSeat(int row, int number, String holder)
            throws SeatUnavailableException {
        Seat seat = findSeat(row, number);
        checkFreeFor(seat, holder);
        seat.sell();
    }

    /** Файлдан жүктегенде сатылған орынды белгілеу. */
    public synchronized void markSold(int row, int number) {
        Seat seat = seats.get(row + "-" + number);
        if (seat != null) {
            seat.sell();
        }
    }

    public synchronized void releaseSeat(int row, int number) {
        Seat seat = seats.get(row + "-" + number);
        if (seat != null) {
            seat.release();
        }
    }

    /** Броньды тек иесі ғана босата алады. */
    public synchronized boolean releaseIfHeldBy(int row, int number, String holder) {
        Seat seat = seats.get(row + "-" + number);
        if (seat != null && seat.getStatus() == SeatStatus.RESERVED && holder.equals(seat.getHolder())) {
            seat.release();
            return true;
        }
        return false;
    }

    private void checkFreeFor(Seat seat, String holder) throws SeatUnavailableException {
        SeatStatus st = seat.getStatus();
        if (st == SeatStatus.SOLD) {
            throw new SeatUnavailableException("Орын сатылып кеткен (қатар " + seat.getRow()
                    + ", орын " + seat.getNumber() + "). Бір орынға бір ғана билет!");
        }
        if (st == SeatStatus.RESERVED && !holder.equals(seat.getHolder())) {
            throw new SeatUnavailableException("Орын басқа адамның брондауында (қатар " + seat.getRow()
                    + ", орын " + seat.getNumber() + ").");
        }
    }

    public synchronized List<Seat> getAvailableSeats() {
        List<Seat> result = new ArrayList<>();
        for (Seat s : seats.values()) {
            if (s.getStatus() == SeatStatus.AVAILABLE) {
                result.add(s);
            }
        }
        return result;
    }

    /** [ ] бос, [R] брондалған, [X] сатылған. */
    public synchronized String renderSeatMap() {
        StringBuilder sb = new StringBuilder("          ЭКРАН\n     ");
        for (int n = 1; n <= hall.getSeatsPerRow(); n++) {
            sb.append(String.format("%3d ", n));
        }
        sb.append('\n');
        for (int r = 1; r <= hall.getRows(); r++) {
            sb.append(String.format("Қат.%2d", r));
            for (int n = 1; n <= hall.getSeatsPerRow(); n++) {
                SeatStatus st = seats.get(r + "-" + n).getStatus();
                String mark = st == SeatStatus.AVAILABLE ? "[ ]" : st == SeatStatus.RESERVED ? "[R]" : "[X]";
                sb.append(' ').append(mark);
            }
            sb.append('\n');
        }
        sb.append("[ ] бос   [R] брондалған   [X] сатылған\n");
        return sb.toString();
    }

    public int getSessionId() { return sessionId; }
    public Movie getMovie() { return movie; }
    public Hall getHall() { return hall; }
    public LocalDateTime getDateTime() { return dateTime; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return "Сеанс #" + sessionId + " | " + movie.getTitle() + " | " + hall.getHallName()
                + " | " + dateTime.format(FMT) + " | " + (int) price + " ₸";
    }
}
