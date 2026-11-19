package kz.kinopark.storage;

import kz.kinopark.model.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Деректерді CSV файлдарына сақтау/оқу (бөлгіш: '|'). */
public class CsvStorage {
    private static final String SEP = "\\|";
    private final Path dir;

    public CsvStorage(Path dir) {
        this.dir = dir;
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String[]> read(String file) {
        List<String[]> rows = new ArrayList<>();
        Path p = dir.resolve(file);
        if (!Files.exists(p)) {
            return rows;
        }
        try {
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {      // 0-жол — тақырып
                if (!lines.get(i).isBlank()) {
                    rows.add(lines.get(i).split(SEP, -1));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return rows;
    }

    private void write(String file, String header, List<String> lines) {
        List<String> all = new ArrayList<>();
        all.add(header);
        all.addAll(lines);
        try {
            Files.write(dir.resolve(file), all, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ---------- movies ----------
    public void saveMovies(Collection<Movie> movies) {
        List<String> lines = new ArrayList<>();
        for (Movie m : movies) {
            lines.add(m.getMovieId() + "|" + clean(m.getTitle()) + "|" + clean(m.getGenre()) + "|" + m.getDuration());
        }
        write("movies.csv", "id|title|genre|duration", lines);
    }

    public List<Movie> loadMovies() {
        List<Movie> list = new ArrayList<>();
        for (String[] c : read("movies.csv")) {
            list.add(new Movie(Integer.parseInt(c[0]), c[1], c[2], Integer.parseInt(c[3])));
        }
        return list;
    }

    // ---------- sessions ----------
    public void saveSessions(Collection<Session> sessions) {
        List<String> lines = new ArrayList<>();
        for (Session s : sessions) {
            lines.add(s.getSessionId() + "|" + s.getMovie().getMovieId() + "|" + s.getHall().getHallId()
                    + "|" + s.getDateTime().format(Session.FMT) + "|" + s.getPrice());
        }
        write("sessions.csv", "id|movieId|hallId|dateTime|price", lines);
    }

    public List<Session> loadSessions(Map<Integer, Movie> movies, Map<Integer, Hall> halls) {
        List<Session> list = new ArrayList<>();
        for (String[] c : read("sessions.csv")) {
            Movie m = movies.get(Integer.parseInt(c[1]));
            Hall h = halls.get(Integer.parseInt(c[2]));
            if (m != null && h != null) {
                list.add(new Session(Integer.parseInt(c[0]), m, h,
                        LocalDateTime.parse(c[3], Session.FMT), Double.parseDouble(c[4])));
            }
        }
        return list;
    }

    // ---------- tickets ----------
    public void saveTickets(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            lines.add(t.getTicketCode() + "|" + t.getSession().getSessionId() + "|" + t.getRow() + "|"
                    + t.getNumber() + "|" + t.getPrice() + "|" + t.getChannel() + "|" + clean(t.getCustomerName())
                    + "|" + t.getPayment().getMethod() + "|" + t.getStatus() + "|" + t.getPayment().getPaymentId());
        }
        write("tickets.csv", "code|sessionId|row|number|price|channel|customer|method|status|paymentId", lines);
    }

    public List<Ticket> loadTickets(Map<Integer, Session> sessions) {
        List<Ticket> list = new ArrayList<>();
        for (String[] c : read("tickets.csv")) {
            Session s = sessions.get(Integer.parseInt(c[1]));
            if (s == null) {
                continue;
            }
            double price = Double.parseDouble(c[4]);
            Payment p = new Payment(Integer.parseInt(c[9]), price, c[7]);
            list.add(new Ticket(c[0], s, Integer.parseInt(c[2]), Integer.parseInt(c[3]),
                    price, c[5], c[6], p, c[8]));
        }
        return list;
    }

    private static String clean(String s) {
        return s.replace("|", "/").replace("\n", " ");
    }
}
