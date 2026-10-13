package com.travelsync.main;

import com.travelsync.analysis.AdvancedAlgorithms;
import com.travelsync.data.OutputWriter;
import com.travelsync.model.Booking;
import com.travelsync.model.Hotel;
import com.travelsync.strings.KMP;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Travel-facing reports built from the current hotels and booking list. */
public final class AdvancedMenu {
    private final Scanner sc;
    private final List<Hotel> hotels;
    private final List<Booking> bookings;

    public AdvancedMenu(Scanner sc, List<Hotel> hotels, List<Booking> bookings) {
        this.sc = sc;
        this.hotels = hotels;
        this.bookings = bookings;
    }

    public void findHotels() {
        System.out.print("Destination: ");
        if (!sc.hasNextLine()) return;
        String city = sc.nextLine().trim();
        System.out.print("Feature or preference (press Enter for any): ");
        if (!sc.hasNextLine()) return;
        String preference = sc.nextLine().trim();
        List<Hotel> found = new ArrayList<>();
        for (Hotel h : hotels) {
            boolean sameCity = h.getCity().equalsIgnoreCase(city);
            boolean feature = preference.isEmpty() || KMP.searchIgnoreCase(h.getDescription(), preference).size() > 0
                    || KMP.searchIgnoreCase(h.getName(), preference).size() > 0;
            if (sameCity && feature) found.add(h);
        }
        if (found.isEmpty()) {
            System.out.println("No available hotel matches that destination and feature.");
            OutputWriter.log("HOTEL_MATCH", "Live hotel data", city + " / " + preference, "no match");
            return;
        }
        System.out.println("\nHotels matching your trip:");
        for (Hotel h : found) {
            String stock = h.getRoomsAvailable() > 0 ? h.getRoomsAvailable() + " rooms available" : "sold out";
            System.out.printf("%-28s Rs %d/night | %s%n", h.getName(), h.getPricePerNight(), stock);
        }
        OutputWriter.log("HOTEL_MATCH", "Live hotel data", city + " / " + preference, found.size() + " matches");
    }

    public void bookingReport() {
        if (bookings.isEmpty()) {
            System.out.println("There are no bookings to report yet.");
            return;
        }
        long[] amounts = new long[bookings.size()];
        for (int i = 0; i < bookings.size(); i++) amounts[i] = bookings.get(i).getAmount();
        long total = AdvancedAlgorithms.parallelReduce(amounts, (a, b) -> a + b, Math.min(4, amounts.length));
        int[] sorted = new int[amounts.length];
        for (int i = 0; i < amounts.length; i++) sorted[i] = Math.toIntExact(amounts[i]);
        AdvancedAlgorithms.quickSort(sorted);
        int sampleIndex = AdvancedAlgorithms.reservoirSample(indexes(bookings.size()), (long) System.nanoTime());
        Booking sample = bookings.get(sampleIndex);
        System.out.println("\nCurrent booking activity");
        System.out.println("Bookings: " + bookings.size());
        System.out.println("Total booked value: Rs " + total);
        System.out.println("Lowest booking: Rs " + sorted[0] + " | Highest booking: Rs " + sorted[sorted.length - 1]);
        System.out.println("Sample booking: " + sample.getCode() + " — " + sample.getDestination() + " — Rs " + sample.getAmount());
        OutputWriter.append("booking_activity.csv", "timestamp,bookingCount,totalAmount,lowestAmount,highestAmount,sampleCode,sampleDestination",
                OutputWriter.now(), String.valueOf(bookings.size()), String.valueOf(total), String.valueOf(sorted[0]),
                String.valueOf(sorted[sorted.length - 1]), sample.getCode(), sample.getDestination());
        OutputWriter.log("BOOKING_REPORT", "Current session bookings", String.valueOf(bookings.size()), "total value Rs " + total);
    }

    private int[] indexes(int n) { int[] a = new int[n]; for (int i = 0; i < n; i++) a[i] = i; return a; }
}
