package kz.kinopark;

import kz.kinopark.service.BookingService;
import kz.kinopark.storage.CsvStorage;
import kz.kinopark.ui.ConsoleApp;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        BookingService service = new BookingService(new CsvStorage(Path.of("data")));
        new ConsoleApp(service).run();
    }
}
