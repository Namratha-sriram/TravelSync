package com.travelsync.data;

import com.travelsync.model.Booking;
import com.travelsync.model.Destination;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Central CSV output helpers. Every field is quoted safely when necessary. */
public final class OutputWriter {
    private static final Path DIR = Path.of("output");
    private OutputWriter() { }

    public static String now() { return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME); }

    private static String csv(String value) {
        String s = value == null ? "" : value;
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    public static synchronized void append(String file, String header, String... fields) {
        try {
            Files.createDirectories(DIR);
            Path p = DIR.resolve(file);
            if (!Files.exists(p) || Files.size(p) == 0) Files.writeString(p, header + System.lineSeparator(), StandardCharsets.UTF_8);
            StringBuilder row = new StringBuilder();
            for (int i = 0; i < fields.length; i++) { if (i > 0) row.append(','); row.append(csv(fields[i])); }
            Files.writeString(p, row + System.lineSeparator(), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) { System.err.println("Could not write output/" + file + ": " + e.getMessage()); }
    }

    public static void log(String action, String algorithm, String input, String result) {
        append("activity_log.csv", "timestamp,action,algorithm,input,result", now(), action, algorithm, input, result);
    }
    public static void saveSearchResults(String query, String algorithm, List<Destination> results) {
        for (Destination d : results) append("search_results.csv", "timestamp,query,algorithm,destination,country", now(), query, algorithm, d.getName(), d.getCountry());
    }
    public static void saveReceipt(Booking b) {
        append("receipts.csv", "timestamp,code,customer,destination,hotel,nights,amount,bookedAt", now(), b.getCode(), b.getCustomer(), b.getDestination(), b.getHotel(), String.valueOf(b.getNights()), String.valueOf(b.getAmount()), b.getBookedAt());
    }
}
