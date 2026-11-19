package kz.kinopark.model;

public class NoDiscount implements Discount {
    @Override
    public double calculateDiscount(double price) {
        return price;
    }

    @Override
    public String getName() {
        return "Жеңілдіксіз";
    }
}
