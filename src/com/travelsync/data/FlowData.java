package com.travelsync.data;

import java.util.ArrayList;
import java.util.List;

// Reads transport.csv and travel_requests.csv
public class FlowData {

    public static class Route {
        public final String from;
        public final String to;
        public final int capacity;      // passengers (seats) per day
        public final String mode;

        public Route(String from, String to, int capacity, String mode) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.mode = mode;
        }
    }

    public static class Request {
        public final String traveler;
        public final String destination;
        public final String preference;   // may be empty

        public Request(String traveler, String destination, String preference) {
            this.traveler = traveler;
            this.destination = destination;
            this.preference = preference;
        }
    }

    // transport.csv: from,to,capacity,mode
    public static List<Route> loadTransport(String path) {
        List<Route> list = new ArrayList<>();
        for (String[] f : CsvUtil.read(path)) {
            if (f.length < 4) continue;
            try {
                int cap = Integer.parseInt(f[2].trim());
                if (cap < 0) continue;
                list.add(new Route(f[0], f[1], cap, f[3]));
            } catch (NumberFormatException e) {
                System.out.println("Skipping transport row with bad capacity: " + f[0] + " -> " + f[1]);
            }
        }
        return list;
    }

    // travel_requests.csv: traveler,destination,preference
    public static List<Request> loadRequests(String path) {
        List<Request> list = new ArrayList<>();
        for (String[] f : CsvUtil.read(path)) {
            if (f.length < 2 || f[0].isEmpty() || f[1].isEmpty()) continue;
            String pref = f.length >= 3 ? f[2] : "";
            list.add(new Request(f[0], f[1], pref));
        }
        return list;
    }
}