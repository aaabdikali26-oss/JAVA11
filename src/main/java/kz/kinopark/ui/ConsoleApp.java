package kz.kinopark.ui;

import kz.kinopark.exception.SeatUnavailableException;
import kz.kinopark.model.*;
import kz.kinopark.service.BookingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {
    private final BookingService service;
    private final Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);

    public ConsoleApp(BookingService service) {
        this.service = service;
    }

    public void run() {
        System.out.println("=== KINOPARK: билет сату жүйесі ===");
        while (true) {
            System.out.println("\n--- БАСТЫ МӘЗІР ---");
            System.out.println("1. Клиент (онлайн)");
            System.out.println("2. Кассир (касса)");
            System.out.println("3. Әкімші");
            System.out.println("4. Демо: онлайн + касса қақтығысы");
            System.out.println("0. Шығу");
            switch (readInt("Таңдаңыз: ")) {
                case 1 -> customerMenu();
                case 2 -> cashierMenu();
                case 3 -> adminMenu();
                case 4 -> conflictDemo();
                case 0 -> {
                    System.out.println("Сау болыңыз! Деректер data/ папкасында сақталды.");
                    return;
                }
                default -> System.out.println("Қате таңдау.");
            }
        }
    }

    // ================= КЛИЕНТ =================
    private void customerMenu() {
        String name = readLine("Атыңыз: ");
        String phone = readLine("Телефон: ");
        Customer user = new Customer(service.nextUserId(), name, phone);
        user.login();
        while (true) {
            System.out.println("\n--- КЛИЕНТ: " + user.getName() + " ---");
            System.out.println("1. Фильмдер тізімі");
            System.out.println("2. Сеанстар тізімі");
            System.out.println("3. Залды және бос орындарды көру");
            System.out.println("4. Орынды брондау (" + BookingService.HOLD_MINUTES + " мин)");
            System.out.println("5. Билет сатып алу (онлайн)");
            System.out.println("6. Броньнан бас тарту");
            System.out.println("7. Менің билеттерім");
            System.out.println("0. Артқа");
            switch (readInt("Таңдаңыз: ")) {
                case 1 -> printMovies();
                case 2 -> printSessions();
                case 3 -> showSeats();
                case 4 -> reserveSeat(user);
                case 5 -> sellTicket(user, user.getName(), "ONLINE");
                case 6 -> cancelReservation(user);
                case 7 -> service.ticketsOf(user.getName()).forEach(System.out::println);
                case 0 -> {
                    user.logout();
                    return;
                }
                default -> System.out.println("Қате таңдау.");
            }
        }
    }

    // ================= КАССИР =================
    private void cashierMenu() {
        Cashier cashier = new Cashier(service.nextUserId(), readLine("Кассир аты: "), "-");
        cashier.login();
        while (true) {
            System.out.println("\n--- КАССИР: " + cashier.getName() + " ---");
            System.out.println("1. Сеанстар тізімі");
            System.out.println("2. Залды және бос орындарды көру");
            System.out.println("3. Орынды тексеру");
            System.out.println("4. Билет сату (касса)");
            System.out.println("0. Артқа");
            switch (readInt("Таңдаңыз: ")) {
                case 1 -> printSessions();
                case 2 -> showSeats();
                case 3 -> {
                    Session s = askSession();
                    if (s != null) {
                        int row = readInt("Қатар: ");
                        int num = readInt("Орын: ");
                        System.out.println(cashier.checkSeat(s, row, num) ? "Орын БОС." : "Орын бос емес.");
                    }
                }
                case 4 -> sellTicket(cashier, readLine("Сатып алушының аты: "), "CASHIER");
                case 0 -> {
                    cashier.logout();
                    return;
                }
                default -> System.out.println("Қате таңдау.");
            }
        }
    }

    // ================= ӘКІМШІ =================
    private void adminMenu() {
        Admin admin = new Admin(service.nextUserId(), readLine("Әкімші аты: "), "-");
        admin.login();
        while (true) {
            System.out.println("\n--- ӘКІМШІ: " + admin.getName() + " ---");
            System.out.println("1. Фильмдер тізімі");
            System.out.println("2. Сеанстар тізімі");
            System.out.println("3. Фильм қосу");
            System.out.println("4. Сеанс қосу");
            System.out.println("5. Барлық билеттер");
            System.out.println("6. Билетті қайтару (бас тарту)");
            System.out.println("0. Артқа");
            switch (readInt("Таңдаңыз: ")) {
                case 1 -> printMovies();
                case 2 -> printSessions();
                case 3 -> {
                    Movie m = service.addMovie(readLine("Атауы: "), readLine("Жанры: "), readInt("Ұзақтығы (мин): "));
                    System.out.println("Қосылды: " + m);
                }
                case 4 -> addSession();
                case 5 -> {
                    List<Ticket> all = service.getTickets();
                    if (all.isEmpty()) System.out.println("Билет жоқ.");
                    all.forEach(System.out::println);
                }
                case 6 -> System.out.println(service.cancelTicket(readLine("Билет коды: "))
                        ? "Билет қайтарылды, орын босады." : "Белсенді билет табылмады.");
                case 0 -> {
                    admin.logout();
                    return;
                }
                default -> System.out.println("Қате таңдау.");
            }
        }
    }

    private void addSession() {
        printMovies();
        Movie m = service.findMovie(readInt("Фильм ID: "));
        service.getHalls().forEach(h -> System.out.println("[" + h.getHallId() + "] " + h));
        Hall h = service.findHall(readInt("Зал ID: "));
        if (m == null || h == null) {
            System.out.println("Фильм немесе зал табылмады.");
            return;
        }
        try {
            LocalDateTime t = LocalDateTime.parse(readLine("Уақыты (yyyy-MM-dd HH:mm): "), Session.FMT);
            Session s = service.addSession(m, h, t, readInt("Бағасы (₸): "));
            System.out.println("Қосылды: " + s);
        } catch (DateTimeParseException e) {
            System.out.println("Уақыт форматы қате.");
        }
    }

    // ================= ОРТАҚ ӘРЕКЕТТЕР =================
    private void reserveSeat(User user) {
        Session s = askSession();
        if (s == null) return;
        System.out.println(s.renderSeatMap());
        try {
            service.reserve(s, readInt("Қатар: "), readInt("Орын: "), user);
            System.out.println("Орын " + BookingService.HOLD_MINUTES + " минутқа брондалды.");
        } catch (SeatUnavailableException e) {
            System.out.println("ҚАТЕ: " + e.getMessage());
        }
    }

    private void cancelReservation(User user) {
        Session s = askSession();
        if (s == null) return;
        boolean ok = service.cancelReservation(s, readInt("Қатар: "), readInt("Орын: "), user);
        System.out.println(ok ? "Бронь алынды." : "Сізде мұндай бронь жоқ.");
    }

    private void sellTicket(User seller, String buyerName, String channel) {
        Session s = askSession();
        if (s == null) return;
        System.out.println(s.renderSeatMap());
        int row = readInt("Қатар: ");
        int num = readInt("Орын: ");
        Discount discount = readLine("Студент жеңілдігі бар ма? (y/n): ").equalsIgnoreCase("y")
                ? new StudentDiscount(20) : new NoDiscount();
        String method = channel.equals("ONLINE")
                ? readLine("Төлем түрі (CARD/KASPI): ") : readLine("Төлем түрі (CASH/CARD): ");
        try {
            Ticket t = service.sell(s, row, num, seller, buyerName, discount, method.toUpperCase(), channel);
            System.out.println("\n✔ Билет сатылды! (" + discount.getName() + ")\n" + t);
        } catch (SeatUnavailableException e) {
            System.out.println("ҚАТЕ: " + e.getMessage());
        }
    }

    private void showSeats() {
        Session s = askSession();
        if (s != null) {
            System.out.println(s);
            System.out.println(s.renderSeatMap());
            System.out.println("Бос орын саны: " + s.getAvailableSeats().size());
        }
    }

    /** Негізгі проблеманың демонстрациясы. */
    private void conflictDemo() {
        System.out.println("\n--- ДЕМО ---");
        Session s = service.getSessions().iterator().next();
        Customer c = new Customer(service.nextUserId(), "Демо клиент", "-");
        Cashier k = new Cashier(service.nextUserId(), "Демо кассир", "-");
        try {
            service.reserve(s, 1, 1, c);
            System.out.println("1) Клиент онлайн 1-қатар 1-орынды брондады.");
            System.out.println("2) Кассир сол орынға билет сатпақшы...");
            service.sell(s, 1, 1, k, "Кассадағы адам", new NoDiscount(), "CASH", "CASHIER");
            System.out.println("Бұл жерге жетпеу керек!");
        } catch (SeatUnavailableException e) {
            System.out.println("3) Жүйе тоқтатты → " + e.getMessage());
        } finally {
            service.cancelReservation(s, 1, 1, c);
            System.out.println("(Демо броні алынды.)");
        }
    }

    private void printMovies() {
        service.getMovies().forEach(System.out::println);
    }

    private void printSessions() {
        service.getSessions().forEach(System.out::println);
    }

    private Session askSession() {
        printSessions();
        Session s = service.findSession(readInt("Сеанс ID: "));
        if (s == null) System.out.println("Сеанс табылмады.");
        return s;
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Сан енгізіңіз.");
            }
        }
    }
}
