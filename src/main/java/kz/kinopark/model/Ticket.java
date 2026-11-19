package kz.kinopark.model;

public class Ticket {
    public static final String ACTIVE = "ACTIVE";
    public static final String CANCELLED = "CANCELLED";

    private final String ticketCode;
    private final Session session;
    private final int row;
    private final int number;
    private final double price;
    private final String channel;      // ONLINE немесе CASHIER
    private final String customerName;
    private final Payment payment;
    private String status;

    public Ticket(String ticketCode, Session session, int row, int number, double price,
                  String channel, String customerName, Payment payment, String status) {
        this.ticketCode = ticketCode;
        this.session = session;
        this.row = row;
        this.number = number;
        this.price = price;
        this.channel = channel;
        this.customerName = customerName;
        this.payment = payment;
        this.status = status;
    }

    public static String generateTicketCode(int n) {
        return String.format("KP%05d", n);
    }

    public void cancelTicket() {
        this.status = CANCELLED;
    }

    public boolean isActive() {
        return ACTIVE.equals(status);
    }

    public String getTicketCode() { return ticketCode; }
    public Session getSession() { return session; }
    public int getRow() { return row; }
    public int getNumber() { return number; }
    public double getPrice() { return price; }
    public String getChannel() { return channel; }
    public String getCustomerName() { return customerName; }
    public Payment getPayment() { return payment; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return ticketCode + " | " + session.getMovie().getTitle() + " | "
                + session.getDateTime().format(Session.FMT) + " | " + session.getHall().getHallName()
                + " | қатар " + row + ", орын " + number + " | " + (int) price + " ₸ | "
                + channel + " | " + customerName + " | " + status;
    }
}
