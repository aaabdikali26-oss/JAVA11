package kz.kinopark.model;

public class StudentDiscount implements Discount {
    private final double percentage;

    public StudentDiscount(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public double calculateDiscount(double price) {
        return price * (1 - percentage / 100.0);
    }

    @Override
    public String getName() {
        return "Студент жеңілдігі (-" + (int) percentage + "%)";
    }
}
