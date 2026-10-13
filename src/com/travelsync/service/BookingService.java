package com.travelsync.service;

import com.travelsync.data.CsvUtil;
import com.travelsync.data.DataLoader;
import com.travelsync.dp.DamerauLevenshtein;
import com.travelsync.model.Booking;
import com.travelsync.model.Destination;
import com.travelsync.model.Hotel;
import com.travelsync.strings.KMP;
import com.travelsync.strings.RabinKarp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookingService {
    private final List<Destination> destinations;
    private final List<Hotel> hotels;
    private final List<Booking> bookings;
    private final String hotelsPath;
    private final String bookingsPath;
    private final List<String> destNames = new ArrayList<>();

    public BookingService(List<Destination> destinations, List<Hotel> hotels, List<Booking> bookings,
                          String hotelsPath, String bookingsPath) {
        this.destinations = destinations;
        this.hotels = hotels;
        this.bookings = bookings;
        this.hotelsPath = hotelsPath;
        this.bookingsPath = bookingsPath;
        for (Destination d : destinations) destNames.add(d.getName());
    }

    // Damerau-Levenshtein: closest destination, or null if nothing is close enough
    public Destination resolveDestination(String input) {
        String best = DamerauLevenshtein.closest(input, destNames);
        if (best == null) return null;
        int dist = DamerauLevenshtein.distance(input, best);
        if (dist > Math.max(2, best.length() / 3)) return null;
        return findDestination(best);
    }

    public Destination findDestination(String name) {
        for (Destination d : destinations) {
            if (d.getName().equalsIgnoreCase(name)) return d;
        }
        return null;
    }

    public List<Hotel> hotelsIn(Destination dest) {
        List<Hotel> result = new ArrayList<>();
        for (Hotel h : hotels) {
            if (h.getCity().equalsIgnoreCase(dest.getName())) result.add(h);
        }
        return result;
    }

    // KMP: hotels whose description matches the preference come first
    public List<Hotel> rankByPreference(List<Hotel> list, String keyword) {
        List<Hotel> matched = new ArrayList<>();
        List<Hotel> others = new ArrayList<>();
        for (Hotel h : list) {
            if (!keyword.isEmpty() && !KMP.searchIgnoreCase(h.getDescription(), keyword).isEmpty())
                matched.add(h);
            else
                others.add(h);
        }
        matched.addAll(others);
        return matched;
    }

    // Rabin-Karp lookup by booking code
    public Booking findByCode(String code) {
        for (Booking b : bookings) {
            if (b.getCode().length() == code.length()
                    && !RabinKarp.search(b.getCode(), code).isEmpty()) return b;
        }
        return null;
    }

    private String nextBookingCode() {
        int max = 1000;
        for (Booking b : bookings) {
            try {
                max = Math.max(max, Integer.parseInt(b.getCode().substring(2)));
            } catch (NumberFormatException ignored) { }
        }
        int next = max + 1;
        String code = "BK" + next;
        while (findByCode(code) != null) code = "BK" + (++next);   // Rabin-Karp uniqueness check
        return code;
    }

    // Reserves a room, creates the booking, saves bookings.csv and hotels.csv.
    // Returns null when the hotel is sold out or nights is not positive.
    public Booking book(String customer, Destination dest, Hotel hotel, int nights) {
        if (nights < 1 || !hotel.takeRoom()) return null;
        String when = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Booking b = new Booking(nextBookingCode(), customer, dest.getName(), hotel.getName(),
                nights, hotel.getPricePerNight() * nights, when);
        bookings.add(b);
        CsvUtil.append(bookingsPath, DataLoader.BOOKING_HEADER, DataLoader.bookingRow(b));
        DataLoader.saveHotels(hotelsPath, hotels);
        return b;
    }

    // KMP lookup by customer name (partial, case-insensitive)
    public List<Booking> findByCustomer(String name) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : bookings) {
            if (!KMP.searchIgnoreCase(b.getCustomer(), name).isEmpty()) result.add(b);
        }
        return result;
    }

    // Cancel: the customer name must match; the room goes back and both CSV files are rewritten
    public boolean cancel(String code, String customer) {
        Booking b = findByCode(code);
        if (b == null || !b.getCustomer().equalsIgnoreCase(customer.trim())) return false;
        bookings.remove(b);
        for (Hotel h : hotels) {
            if (h.getName().equalsIgnoreCase(b.getHotel())) {
                h.releaseRoom();
                break;
            }
        }
        DataLoader.saveBookings(bookingsPath, bookings);
        DataLoader.saveHotels(hotelsPath, hotels);
        return true;
    }
}