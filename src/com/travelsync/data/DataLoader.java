package com.travelsync.data;

import com.travelsync.model.Booking;
import com.travelsync.model.Destination;
import com.travelsync.model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    public static final String DEST_HEADER = "name,country,description,attractions";
    public static final String HOTEL_HEADER = "name,city,description,pricePerNight,roomsAvailable";
    public static final String BOOKING_HEADER = "code,customer,destination,hotel,nights,amount,bookedAt";

    private static Integer toInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // destinations.csv: name,country,description,attractions (attractions separated by ;)
    public static List<Destination> loadDestinations(String path) {
        List<Destination> list = new ArrayList<>();
        for (String[] f : CsvUtil.read(path)) {
            if (f.length < 4) continue;
            List<String> attractions = new ArrayList<>();
            for (String a : f[3].split(";")) {
                if (!a.trim().isEmpty()) attractions.add(a.trim());
            }
            list.add(new Destination(f[0], f[1], f[2], attractions));
        }
        return list;
    }

    // hotels.csv: name,city,description,pricePerNight,roomsAvailable
    public static List<Hotel> loadHotels(String path) {
        List<Hotel> list = new ArrayList<>();
        for (String[] f : CsvUtil.read(path)) {
            if (f.length < 5) continue;
            Integer price = toInt(f[3]);
            Integer rooms = toInt(f[4]);
            if (price == null || rooms == null) {
                System.out.println("Skipping hotel row with bad numbers: " + f[0]);
                continue;
            }
            list.add(new Hotel(f[0], f[1], f[2], price, rooms));
        }
        return list;
    }

    // bookings.csv: code,customer,destination,hotel,nights,amount,bookedAt
    public static List<Booking> loadBookings(String path) {
        List<Booking> list = new ArrayList<>();
        for (String[] f : CsvUtil.read(path)) {
            if (f.length < 7) continue;
            Integer nights = toInt(f[4]);
            Integer amount = toInt(f[5]);
            if (nights == null || amount == null) {
                System.out.println("Skipping booking row with bad numbers: " + f[0]);
                continue;
            }
            list.add(new Booking(f[0], f[1], f[2], f[3], nights, amount, f[6]));
        }
        return list;
    }

    // descriptions.csv: one column "description"
    public static List<String> loadDescriptions(String path) {
        List<String> list = new ArrayList<>();
        for (String[] f : CsvUtil.read(path)) {
            if (f.length >= 1 && !f[0].isEmpty()) list.add(f[0]);
        }
        return list;
    }

    // Write the current state back to CSV
    public static void saveHotels(String path, List<Hotel> hotels) {
        List<String> rows = new ArrayList<>();
        for (Hotel h : hotels) {
            rows.add(CsvUtil.join(h.getName(), h.getCity(), h.getDescription(),
                    String.valueOf(h.getPricePerNight()), String.valueOf(h.getRoomsAvailable())));
        }
        CsvUtil.write(path, HOTEL_HEADER, rows);
    }

    public static String bookingRow(Booking b) {
        return CsvUtil.join(b.getCode(), b.getCustomer(), b.getDestination(), b.getHotel(),
                String.valueOf(b.getNights()), String.valueOf(b.getAmount()), b.getBookedAt());
    }

    public static void saveBookings(String path, List<Booking> bookings) {
        List<String> rows = new ArrayList<>();
        for (Booking b : bookings) rows.add(bookingRow(b));
        CsvUtil.write(path, BOOKING_HEADER, rows);
    }
}