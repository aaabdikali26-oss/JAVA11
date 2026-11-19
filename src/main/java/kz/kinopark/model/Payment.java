package kz.kinopark.model;

public class Payment {
    private final int paymentId;
    private final double amount;
    private final String method;

    public Payment(int paymentId, double amount, String method) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.method = method;
    }

    /** Оқу жобасы үшін төлем әрдайым сәтті деп есептеледі (қарапайым имитация). */
    public boolean processPayment() {
        return amount >= 0;
    }

    public int getPaymentId() { return paymentId; }
    public double getAmount() { return amount; }
    public String getMethod() { return method; }
}
