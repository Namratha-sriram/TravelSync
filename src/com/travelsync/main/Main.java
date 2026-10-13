package com.travelsync.main;

import com.travelsync.data.DataLoader;
import com.travelsync.data.OutputWriter;
import com.travelsync.dp.DamerauLevenshtein;
import com.travelsync.dp.ItineraryBitmaskDP;
import com.travelsync.model.Booking;
import com.travelsync.model.Destination;
import com.travelsync.model.Hotel;
import com.travelsync.service.BookingService;
import com.travelsync.service.PopularityService;
import com.travelsync.service.SearchService;
import com.travelsync.strings.KMP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    static final String DEST_FILE = "data/destinations.csv";
    static final String HOTEL_FILE = "data/hotels.csv";
    static final String BOOKING_FILE = "data/bookings.csv";
    static final String DESC_FILE = "data/descriptions.csv";

    static List<Destination> destinations;
    static List<Hotel> hotels;
    static List<Booking> bookings;
    static BookingService bookingService;
    static SearchService searchService;
    static PopularityService popularity;
    static Scanner sc = new Scanner(System.in);

    // general words that mean "show me everything"
    static final List<String> GENERIC = Arrays.asList(
            "place", "places", "destination", "destinations", "activity", "activities",
            "theme", "themes", "trip", "trips", "travel", "holiday", "holidays",
            "vacation", "all", "any", "anywhere", "everything", "show", "list", "explore");

    public static void main(String[] args) {
        destinations = DataLoader.loadDestinations(DEST_FILE);
        hotels = DataLoader.loadHotels(HOTEL_FILE);
        bookings = DataLoader.loadBookings(BOOKING_FILE);
        List<String> descriptions = DataLoader.loadDescriptions(DESC_FILE);

        if (destinations.isEmpty() || hotels.isEmpty()) {
            System.out.println("Could not load data. Check that the CSV files are inside the 'data' folder");
            System.out.println("and that Eclipse runs the project from its root folder.");
            return;
        }

        bookingService = new BookingService(destinations, hotels, bookings, HOTEL_FILE, BOOKING_FILE);
        searchService = new SearchService(destinations, hotels);
        popularity = new PopularityService(destinations, bookings);
        AdminPanel admin = new AdminPanel(sc, destinations, bookings, descriptions);
        FlowMenu flow = new FlowMenu(sc, hotels);
        AdvancedMenu advanced = new AdvancedMenu(sc, hotels, bookings);
        System.out.println("Loaded " + destinations.size() + " destinations, " + hotels.size()
                + " hotels, " + bookings.size() + " bookings from CSV.");

        while (true) {
            System.out.println("\n=====================================");
            System.out.println("     TravelSync - Travel Planner");
            System.out.println("=====================================");
            System.out.println(" 1. Search destinations");
            System.out.println(" 2. Smart trip finder");
            System.out.println(" 3. Destinations like the one I love");
            System.out.println(" 4. Search hotels");
            System.out.println(" 5. Book a hotel");
            System.out.println(" 6. My bookings");
            System.out.println(" 7. Cancel a booking");
            System.out.println(" 8. Plan a multi-city trip");
            System.out.println(" 9. Trending destinations");
            System.out.println("10. Admin panel");
            System.out.println("11. Transport and room availability");
            System.out.println("12. Find hotels by destination and feature");
            System.out.println("13. Booking activity and totals");
            System.out.println(" 0. Exit");
            System.out.print("Choice: ");

            if (!sc.hasNextLine()) break;
            int ch;
            try {
                ch = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a number.");
                continue;
            }
            if (ch == 0) break;

            switch (ch) {
                case 1 -> searchDestinations();
                case 2 -> smartTripFinder();
                case 3 -> similarDestinations();
                case 4 -> searchHotels();
                case 5 -> bookTrip();
                case 6 -> myBookings();
                case 7 -> cancelBooking();
                case 8 -> planMultiCity();
                case 9 -> popularity.show();
                case 10 -> admin.run();
                case 11 -> flow.run();
                case 12 -> advanced.findHotels();
                case 13 -> advanced.bookingReport();
                default -> System.out.println("Invalid choice.");
            }
        }
        System.out.println("Thank you for using TravelSync! Your results are saved in the 'output' folder.");
    }

    static String ask(String prompt) {
        System.out.print(prompt);
        return sc.hasNextLine() ? sc.nextLine().trim() : "";
    }

    // returns the number typed, or -1 if it is not a number in [min, max]
    static int askInt(String prompt, int min, int max) {
        try {
            int v = Integer.parseInt(ask(prompt));
            return (v >= min && v <= max) ? v : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // 1. Search destinations: never ends with a dead "not found"
    static void searchDestinations() {
        String q = ask("Search destinations (try: beach, snow, temple, Goa, luxury) or press Enter to browse all: ");

        List<Destination> results = new ArrayList<>();
        String note;
        String algo;
        boolean targeted = true;     // false when we are just listing everything

        if (q.isEmpty() || GENERIC.contains(q.toLowerCase())) {
            results.addAll(destinations);
            note = "Here are all our destinations:";
            algo = "Browse all";
            targeted = false;
        } else {
            // step 1: KMP over name, country, description and attractions
            algo = "KMP";
            for (Destination d : destinations) {
                String text = d.getName() + " " + d.getCountry() + " " + d.getDescription()
                        + " " + String.join(" ", d.getAttractions());
                if (!KMP.searchIgnoreCase(text, q).isEmpty()) results.add(d);
            }
            note = "Destinations matching \"" + q + "\":";

            if (results.isEmpty()) {
                // step 2: typo correction with Damerau-Levenshtein
                Destination guess = bookingService.resolveDestination(q);
                if (guess != null) {
                    results.add(guess);
                    algo = "Damerau-Levenshtein";
                    note = "Showing results for " + guess.getName() + " (closest match to \"" + q + "\"):";
                }
            }

            if (results.isEmpty()) {
                // step 3: travel themes found inside a sentence (Aho-Corasick)
                for (SearchService.Match m : searchService.recommend(q)) results.add(m.destination);
                if (!results.isEmpty()) {
                    algo = "Aho-Corasick";
                    note = "Based on the travel themes in your search:";
                }
            }

            if (results.isEmpty()) {
                // step 4: friendly fallback, show everything
                results.addAll(destinations);
                algo = "Browse all (fallback)";
                note = "We couldn't find \"" + q + "\", so here are all our destinations to explore:";
                targeted = false;
            }
        }

        System.out.println("\n" + note);
        for (int i = 0; i < results.size(); i++) {
            Destination d = results.get(i);
            System.out.println((i + 1) + ". " + d);
            System.out.println("     " + d.getDescription());
            if (targeted) popularity.record(d.getName());
        }
        if (!targeted) {
            System.out.println("\nPopular themes to search: " + SearchService.THEMES);
        }

        OutputWriter.saveSearchResults(q, algo, results);
        OutputWriter.log("SEARCH", algo, q, results.size() + " destinations");

        String pick = ask("\nEnter a number to see hotels and book, or press Enter to go back: ");
        if (pick.isEmpty()) return;
        try {
            int n = Integer.parseInt(pick);
            if (n >= 1 && n <= results.size()) bookFor(results.get(n - 1));
            else System.out.println("Invalid choice.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice.");
        }
    }

    // 2. Aho-Corasick finds the themes in a free-text sentence
    static void smartTripFinder() {
        String q = ask("Describe your dream trip (e.g. luxury beach holiday with nightlife): ");
        List<SearchService.Match> results = searchService.recommend(q);
        if (results.isEmpty()) {
            System.out.println("No travel themes spotted. Try words like: " + SearchService.THEMES);
            OutputWriter.log("SMART_FINDER", "Aho-Corasick", q, "no themes");
            return;
        }
        int shown = Math.min(3, results.size());
        List<Destination> top = new ArrayList<>();
        System.out.println("\nBest matches for you:");
        for (int i = 0; i < shown; i++) {
            SearchService.Match m = results.get(i);
            top.add(m.destination);
            System.out.println((i + 1) + ". " + m.destination.getName() + " - matches " + m.matched);
            System.out.println("   " + m.destination.getDescription());
        }
        OutputWriter.saveSearchResults(q, "Aho-Corasick", top);
        OutputWriter.log("SMART_FINDER", "Aho-Corasick", q, shown + " destinations");

        String pick = ask("Enter a number to book it, or press Enter to go back: ");
        if (pick.isEmpty()) return;
        try {
            int n = Integer.parseInt(pick);
            if (n >= 1 && n <= shown) bookFor(top.get(n - 1));
            else System.out.println("Invalid choice.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice.");
        }
    }

    // 3. LCP / Kasai finds destinations with the most similar descriptions
    static void similarDestinations() {
        String input = ask("Which destination do you like? ");
        Destination dest = bookingService.resolveDestination(input);
        if (dest == null) {
            System.out.println("Sorry, no destination like that. Available: " + destinationNames());
            return;
        }
        List<SearchService.Similar> list = searchService.similarTo(dest, 3);
        if (list.isEmpty()) {
            System.out.println("No similar destinations found.");
            return;
        }
        System.out.println("If you like " + dest.getName() + ", you may also enjoy:");
        List<Destination> shown = new ArrayList<>();
        for (SearchService.Similar s : list) {
            shown.add(s.destination);
            System.out.println("- " + s.destination.getName() + "  (both have: \"" + s.commonPhrase + "\")");
        }
        OutputWriter.saveSearchResults(dest.getName(), "LCP/Kasai", shown);
        OutputWriter.log("SIMILAR", "LCP/Kasai", dest.getName(), shown.size() + " destinations");
    }

    static void printHotel(int number, Hotel h) {
        String stock = h.hasRoom() ? h.getRoomsAvailable() + " rooms left" : "SOLD OUT";
        System.out.println(number + ". " + h + " - Rs " + h.getPricePerNight() + "/night, " + stock);
        System.out.println("     " + h.getDescription());
    }

    // 4. Suffix Array search over hotels, then optional booking
    static void searchHotels() {
        String q = ask("Search hotels (name, city or feature, e.g. luxury): ");
        List<Hotel> found = searchService.searchHotels(q);
        String algo = "Suffix Array";
        if (found.isEmpty()) {
            System.out.println("No exact match, so here are all our hotels:");
            found = hotels;
            algo = "Browse all (fallback)";
        }
        for (int i = 0; i < found.size(); i++) printHotel(i + 1, found.get(i));
        OutputWriter.log("HOTEL_SEARCH", algo, q, found.size() + " hotels");

        String pick = ask("Enter a number to book, or press Enter to go back: ");
        if (pick.isEmpty()) return;
        try {
            int n = Integer.parseInt(pick);
            if (n < 1 || n > found.size()) {
                System.out.println("Invalid choice.");
                return;
            }
            Hotel h = found.get(n - 1);
            Destination dest = bookingService.findDestination(h.getCity());
            if (dest == null) System.out.println("Destination details not available.");
            else completeBooking(dest, h);
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice.");
        }
    }

    // 5. Book a hotel
    static void bookTrip() {
        String input = ask("Where do you want to go? (" + destinationNames() + "): ");
        Destination dest = bookingService.resolveDestination(input);
        if (dest == null) {
            System.out.println("Sorry, no destination like that. Available: " + destinationNames());
            return;
        }
        if (!dest.getName().equalsIgnoreCase(input)) {
            String yn = ask("Did you mean " + dest.getName() + "? (y/n): ");
            if (!yn.equalsIgnoreCase("y")) return;
        }
        bookFor(dest);
    }

    static void bookFor(Destination dest) {
        popularity.record(dest.getName());
        System.out.println("\n" + dest);
        System.out.println("About: " + dest.getDescription());
        System.out.println("Highlights: " + searchService.highlights(dest.getDescription()));

        List<Hotel> options = bookingService.hotelsIn(dest);
        if (options.isEmpty()) {
            System.out.println("No hotels available here yet.");
            return;
        }
        String pref = ask("Any preference (e.g. luxury, beach, or press Enter to skip): ");
        options = bookingService.rankByPreference(options, pref);
        System.out.println("\nAvailable hotels:");
        for (int i = 0; i < options.size(); i++) printHotel(i + 1, options.get(i));

        int pick = askInt("Choose hotel number: ", 1, options.size());
        if (pick < 0) {
            System.out.println("Invalid choice.");
            return;
        }
        completeBooking(dest, options.get(pick - 1));
    }

    static void completeBooking(Destination dest, Hotel hotel) {
        if (!hotel.hasRoom()) {
            System.out.println("Sorry, " + hotel.getName() + " is sold out. Please choose another hotel.");
            return;
        }
        String customer = ask("Your name: ");
        if (customer.isEmpty()) {
            System.out.println("Name is required.");
            return;
        }
        int nights = askInt("Number of nights (1-30): ", 1, 30);
        if (nights < 0) {
            System.out.println("Please enter a number of nights between 1 and 30.");
            return;
        }
        Booking b = bookingService.book(customer, dest, hotel, nights);
        if (b == null) {
            System.out.println("Booking failed. The hotel may have just sold out.");
            return;
        }
        popularity.record(dest.getName());
        OutputWriter.saveReceipt(b);
        OutputWriter.log("BOOK", "Rabin-Karp (code check)", customer + " / " + hotel.getName(), b.getCode());

        System.out.println("\n=== Booking confirmed ===");
        System.out.println("Code:        " + b.getCode());
        System.out.println("Customer:    " + b.getCustomer());
        System.out.println("Destination: " + b.getDestination());
        System.out.println("Hotel:       " + b.getHotel());
        System.out.println("Nights:      " + b.getNights());
        System.out.println("Total:       Rs " + b.getAmount());
        System.out.println("Rooms left:  " + hotel.getRoomsAvailable());
        System.out.println("(Saved to data/bookings.csv and output/booking_receipts.csv)");
    }

    // 6. Rabin-Karp by code, KMP by name
    static void myBookings() {
        String q = ask("Enter booking code (BKxxxx) or customer name: ");
        if (q.isEmpty()) return;
        Booking byCode = bookingService.findByCode(q.toUpperCase());
        if (byCode != null) {
            System.out.println("Found: " + byCode);
            OutputWriter.log("LOOKUP", "Rabin-Karp", q, byCode.getCode());
            return;
        }
        List<Booking> list = bookingService.findByCustomer(q);
        if (list.isEmpty()) System.out.println("No bookings found for \"" + q + "\". Check the code or name and try again.");
        for (Booking b : list) System.out.println(b);
        OutputWriter.log("LOOKUP", "KMP", q, list.size() + " bookings");
    }

    // 7. Cancel
    static void cancelBooking() {
        String code = ask("Booking code to cancel: ").toUpperCase();
        Booking b = bookingService.findByCode(code);
        if (b == null) {
            System.out.println("No booking found with that code.");
            return;
        }
        System.out.println("Found: " + b);
        String name = ask("Confirm the customer name on this booking: ");
        String yn = ask("Cancel this booking? (y/n): ");
        if (!yn.equalsIgnoreCase("y")) {
            System.out.println("Cancellation aborted.");
            return;
        }
        if (bookingService.cancel(code, name)) {
            System.out.println("Booking " + code + " cancelled. The room is available again.");
            OutputWriter.log("CANCEL", "Rabin-Karp", code, "cancelled");
        } else {
            System.out.println("Name does not match this booking. Nothing was cancelled.");
        }
    }

    // 8. Bitmask DP best route
    static void planMultiCity() {
        String[] all = {"Delhi", "Jaipur", "Manali", "Goa", "Kerala"};
        int[][] d = {
            { 0,  5, 12, 26, 45},
            { 5,  0, 16, 22, 40},
            {12, 16,  0, 38, 55},
            {26, 22, 38,  0, 12},
            {45, 40, 55, 12,  0}
        };
        List<String> candidates = Arrays.asList(all).subList(1, all.length);
        System.out.println("Starting from Delhi. Available cities: " + candidates);
        String in = ask("Which cities do you want to visit (comma separated)? ");

        List<Integer> chosen = new ArrayList<>();
        chosen.add(0);
        for (String part : in.split(",")) {
            String name = part.trim();
            if (name.isEmpty()) continue;
            String best = DamerauLevenshtein.closest(name, candidates);
            if (best == null || DamerauLevenshtein.distance(name, best) > 2) {
                System.out.println("Skipping unknown city: " + name);
                continue;
            }
            int idx = Arrays.asList(all).indexOf(best);
            if (!chosen.contains(idx)) chosen.add(idx);
        }
        if (chosen.size() < 2) {
            System.out.println("Choose at least one city from: " + candidates);
            return;
        }

        int k = chosen.size();
        int[][] sub = new int[k][k];
        for (int i = 0; i < k; i++)
            for (int j = 0; j < k; j++)
                sub[i][j] = d[chosen.get(i)][chosen.get(j)];

        ItineraryBitmaskDP.Result r = ItineraryBitmaskDP.shortestPath(sub, 0);
        StringBuilder sb = new StringBuilder();
        for (int c : r.path) sb.append(all[chosen.get(c)]).append(" -> ");
        String route = sb.substring(0, sb.length() - 4);
        System.out.println("\nRecommended route: " + route);
        System.out.println("Total travel time: " + r.cost + " hours");
        System.out.println("Use 'Book a hotel' to reserve stays in these cities.");

        OutputWriter.append("itineraries.csv", "timestamp,requested,route,totalHours",
                OutputWriter.now(), in, route, String.valueOf(r.cost));
        OutputWriter.log("ITINERARY", "Bitmask DP", in, route + " (" + r.cost + "h)");
    }

    static String destinationNames() {
        List<String> names = new ArrayList<>();
        for (Destination d : destinations) names.add(d.getName());
        return String.join(", ", names);
    }
}
