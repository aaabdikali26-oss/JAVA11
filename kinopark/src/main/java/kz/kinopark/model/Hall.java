package kz.kinopark.model;

import java.util.ArrayList;
import java.util.List;

public class Hall {
    private final int hallId;
    private final String hallName;
    private final int rows;
    private final int seatsPerRow;

    public Hall(int hallId, String hallName, int rows, int seatsPerRow) {
        this.hallId = hallId;
        this.hallName = hallName;
        this.rows = rows;
        this.seatsPerRow = seatsPerRow;
    }

    /** Залдың орналасуы бойынша жаңа орындар тізімін жасайды (композиция: Hall *-- Seat). */
    public List<Seat> createSeats() {
        List<Seat> seats = new ArrayList<>();
        for (int r = 1; r <= rows; r++) {
            for (int n = 1; n <= seatsPerRow; n++) {
                seats.add(new Seat(r, n));
            }
        }
        return seats;
    }

    public int getHallId() { return hallId; }
    public String getHallName() { return hallName; }
    public int getRows() { return rows; }
    public int getSeatsPerRow() { return seatsPerRow; }

    @Override
    public String toString() {
        return hallName + " (" + rows + "x" + seatsPerRow + ")";
    }
}
