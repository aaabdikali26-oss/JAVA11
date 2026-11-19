package kz.kinopark.model;

/** АБСТРАКТ КЛАСС: барлық пайдаланушыларға ортақ (abstraction + inheritance). */
public abstract class User {
    private final int id;
    private final String name;
    private final String phone;
    private boolean loggedIn;

    protected User(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    /** Әр актор өз рөлін өзінше қайтарады (polymorphism). */
    public abstract String getRole();

    public boolean login() {
        loggedIn = true;
        return loggedIn;
    }

    public void logout() {
        loggedIn = false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    /** Орынды кім ұстап тұрғанын анықтайтын бірегей белгі. */
    public String getHolderId() {
        return getRole() + "#" + id;
    }

    @Override
    public String toString() {
        return getRole() + ": " + name;
    }
}
