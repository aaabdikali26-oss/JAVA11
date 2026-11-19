package kz.kinopark.exception;

/** Орын бос емес / жоқ болған кездегі өз ерекше жағдайымыз (custom checked exception). */
public class SeatUnavailableException extends Exception {
    public SeatUnavailableException(String message) {
        super(message);
    }
}
