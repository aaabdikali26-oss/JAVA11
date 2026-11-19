package kz.kinopark.model;

public class Seat {
    private final int row;
    private final int number;
    private SeatStatus status = SeatStatus.AVAILABLE;
    private String holder;
    private long reservedUntil;

    public Seat(int row, int number) {
        this.row = row;
        this.number = number;
    }

    /** Брондау уақыты біткен болса, орын өздігінен босайды. */
    public SeatStatus getStatus() {
        if (status == SeatStatus.RESERVED && System.currentTimeMillis() > reservedUntil) {
            release();
        }
        return status;
    }

    public void reserve(String holder, int minutes) {
        this.status = SeatStatus.RESERVED;
        this.holder = holder;
        this.reservedUntil = System.currentTimeMillis() + minutes * 60_000L;
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
        this.holder = null;
        this.reservedUntil = 0;
    }

    public void sell() {
        this.status = SeatStatus.SOLD;
        this.holder = null;
        this.reservedUntil = 0;
    }

    public int getRow() { return row; }
    public int getNumber() { return number; }
    public String getHolder() { return holder; }

    public String getKey() {
        return row + "-" + number;
    }
}
