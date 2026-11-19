package kz.kinopark.model;

public class Customer extends User {
    public Customer(int id, String name, String phone) {
        super(id, name, phone);
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }
}
