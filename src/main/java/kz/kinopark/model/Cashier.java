package kz.kinopark.model;

public class Cashier extends User {
    public Cashier(int id, String name, String phone) {
        super(id, name, phone);
    }

    @Override
    public String getRole() {
        return "CASHIER";
    }

    /** Орын бос па, соны тексереді. */
    public boolean checkSeat(Session session, int row, int number) {
        return session.isSeatAvailable(row, number);
    }
}
