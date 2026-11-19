package kz.kinopark.model;

public class Movie {
    private final int movieId;
    private final String title;
    private final String genre;
    private final int duration;

    public Movie(int movieId, String title, String genre, int duration) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
    }

    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }

    public String getMovieInfo() {
        return "[" + movieId + "] " + title + " | " + genre + " | " + duration + " мин";
    }

    @Override
    public String toString() {
        return getMovieInfo();
    }
}
