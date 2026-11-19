package kz.kinopark.model;

public class Admin extends User {
    public Admin(int id, String name, String phone) {
        super(id, name, phone);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
