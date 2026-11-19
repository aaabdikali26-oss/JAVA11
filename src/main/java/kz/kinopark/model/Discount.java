package kz.kinopark.model;

/** ИНТЕРФЕЙС: жеңілдік стратегиясы (полиморфизм). */
public interface Discount {
    double calculateDiscount(double price);

    String getName();
}
