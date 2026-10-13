package com.travelsync.main;

import com.travelsync.data.FlowData;
import com.travelsync.data.OutputWriter;
import com.travelsync.dp.DamerauLevenshtein;
import com.travelsync.flow.BipartiteMatching;
import com.travelsync.flow.Dinic;
import com.travelsync.flow.EdmondsKarp;
import com.travelsync.flow.FlowGraph;
import com.travelsync.flow.FlowResult;
import com.travelsync.flow.FordFulkerson;
import com.travelsync.flow.MinCut;
import com.travelsync.model.Hotel;
import com.travelsync.service.TransportService;
import com.travelsync.strings.KMP;

import java.util.List;
import java.util.Scanner;

// Operations menu (Module 4): transport capacity, bottlenecks and traveller-to-hotel allocation
public class FlowMenu {
    private static final String TRANSPORT_FILE = "data/transport.csv";
    private static final String REQUEST_FILE = "data/travel_requests.csv";

    private final Scanner sc;
    private final List<Hotel> hotels;
    private final TransportService transport;

    public FlowMenu(Scanner sc, List<Hotel> hotels) {
        this.sc = sc;
        this.hotels = hotels;
        this.transport = new TransportService(FlowData.loadTransport(TRANSPORT_FILE));
    }

    public void run() {
        while (true) {
            System.out.println("\n----- Operations: Transport and Allocation -----");
            System.out.println(" 1. Show transport network");
            System.out.println(" 2. Maximum passengers per day between two cities");
            System.out.println(" 3. Find transport bottlenecks");
            System.out.println(" 4. Allocate travellers to hotel rooms");
            System.out.println(" 0. Back");
            System.out.print("Choice: ");

            if (!sc.hasNextLine()) return;
            int ch;
            try {
                ch = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a number.");
                continue;
            }
            if (ch == 0) return;

            switch (ch) {
                case 1 -> showNetwork();
                case 2 -> maxPassengers();
                case 3 -> bottlenecks();
                case 4 -> allocate();
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return sc.hasNextLine() ? sc.nextLine().trim() : "";
    }

    private boolean networkReady() {
        if (transport.routes().isEmpty()) {
            System.out.println("No transport data found. Check " + TRANSPORT_FILE);
            return false;
        }
        return true;
    }

    // typo-tolerant city name (Damerau-Levenshtein); returns -1 if nothing is close
    private int askCity(String prompt) {
        String in = ask(prompt);
        if (in.isEmpty()) return -1;
        String best = DamerauLevenshtein.closest(in, transport.cities());
        if (best == null || DamerauLevenshtein.distance(in, best) > 2) {
            System.out.println("Unknown city. Available: " + transport.cities());
            return -1;
        }
        if (!best.equalsIgnoreCase(in)) System.out.println("Using " + best);
        return transport.index(best);
    }

    private void showNetwork() {
        if (!networkReady()) return;
        System.out.println("\nCities: " + transport.cities());
        System.out.printf("%-12s -> %-12s %10s  %s%n", "From", "To", "Seats/day", "Mode");
        for (FlowData.Route r : transport.routes()) {
            System.out.printf("%-12s -> %-12s %10d  %s%n", r.from, r.to, r.capacity, r.mode);
        }
    }

    private void maxPassengers() {
        if (!networkReady()) return;
        int s = askCity("From city: ");
        if (s < 0) return;
        int t = askCity("To city: ");
        if (t < 0) return;
        if (s == t) {
            System.out.println("Choose two different cities.");
            return;
        }
        String from = transport.cities().get(s);
        String to = transport.cities().get(t);
        FlowGraph g = transport.graph();

        long t0 = System.nanoTime();
        FlowResult ff = FordFulkerson.maxFlow(g, s, t);
        long t1 = System.nanoTime();
        FlowResult ek = EdmondsKarp.maxFlow(g, s, t);
        long t2 = System.nanoTime();
        FlowResult dn = Dinic.maxFlow(g, s, t);
        long t3 = System.nanoTime();

        System.out.println("\nMaximum passengers per day from " + from + " to " + to + ": " + dn.flow);
        System.out.printf("%-16s %9s %8s %10s%n", "Algorithm", "Max flow", "Steps", "Time (us)");
        System.out.printf("%-16s %9d %8d %10.1f%n", "Ford-Fulkerson", ff.flow, ff.steps, (t1 - t0) / 1000.0);
        System.out.printf("%-16s %9d %8d %10.1f%n", "Edmonds-Karp", ek.flow, ek.steps, (t2 - t1) / 1000.0);
        System.out.printf("%-16s %9d %8d %10.1f%n", "Dinic", dn.flow, dn.steps, (t3 - t2) / 1000.0);
        System.out.println("(Steps = augmenting paths for Ford-Fulkerson and Edmonds-Karp, phases for Dinic)");
        if (ff.flow != ek.flow || ek.flow != dn.flow) System.out.println("Warning: the algorithms disagree!");

        String ts = OutputWriter.now();
        String header = "timestamp,source,sink,algorithm,maxFlow,steps,timeMicros";
        OutputWriter.append("flow_results.csv", header, ts, from, to, "Ford-Fulkerson",
                String.valueOf(ff.flow), String.valueOf(ff.steps), String.format("%.1f", (t1 - t0) / 1000.0));
        OutputWriter.append("flow_results.csv", header, ts, from, to, "Edmonds-Karp",
                String.valueOf(ek.flow), String.valueOf(ek.steps), String.format("%.1f", (t2 - t1) / 1000.0));
        OutputWriter.append("flow_results.csv", header, ts, from, to, "Dinic",
                String.valueOf(dn.flow), String.valueOf(dn.steps), String.format("%.1f", (t3 - t2) / 1000.0));
        OutputWriter.log("MAX_FLOW", "Ford-Fulkerson / Edmonds-Karp / Dinic", from + " -> " + to,
                dn.flow + " passengers/day");
    }

    private void bottlenecks() {
        if (!networkReady()) return;
        int s = askCity("From city: ");
        if (s < 0) return;
        int t = askCity("To city: ");
        if (t < 0) return;
        if (s == t) {
            System.out.println("Choose two different cities.");
            return;
        }
        String from = transport.cities().get(s);
        String to = transport.cities().get(t);

        MinCut cut = MinCut.compute(transport.graph(), s, t);
        System.out.println("\nBottleneck between " + from + " and " + to + " (min-cut = " + cut.capacity + " passengers/day):");
        if (cut.cutEdges.length == 0) System.out.println("  No route exists between these cities.");
        for (int e : cut.cutEdges) {
            FlowData.Route r = transport.routes().get(e / 2);
            System.out.println("  " + r.from + " -> " + r.to + "  (" + r.capacity + " seats/day, " + r.mode + ")");
            OutputWriter.append("bottlenecks.csv", "timestamp,source,sink,linkFrom,linkTo,capacity,mode",
                    OutputWriter.now(), from, to, r.from, r.to, String.valueOf(r.capacity), r.mode);
        }
        System.out.println("Adding seats on these links is the only way to carry more passengers.");
        OutputWriter.log("MIN_CUT", "Dinic + residual BFS", from + " -> " + to,
                cut.cutEdges.length + " bottleneck links, capacity " + cut.capacity);
    }

    private boolean eligible(FlowData.Request q, Hotel h) {
        if (!h.getCity().equalsIgnoreCase(q.destination)) return false;
        return q.preference.isEmpty() || !KMP.searchIgnoreCase(h.getDescription(), q.preference).isEmpty();
    }

    private void allocate() {
        List<FlowData.Request> reqs = FlowData.loadRequests(REQUEST_FILE);
        if (reqs.isEmpty()) {
            System.out.println("No travel requests found. Check " + REQUEST_FILE);
            return;
        }
        int n = reqs.size(), m = hotels.size();
        boolean[][] ok = new boolean[n][m];
        int[] rooms = new int[m];
        for (int j = 0; j < m; j++) rooms[j] = hotels.get(j).getRoomsAvailable();
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                ok[i][j] = eligible(reqs.get(i), hotels.get(j));

        BipartiteMatching.Result res = BipartiteMatching.assign(ok, rooms);

        // greedy first-come baseline, to compare with the flow-based matching
        int[] left = rooms.clone();
        int greedy = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (ok[i][j] && left[j] > 0) {
                    left[j]--;
                    greedy++;
                    break;
                }
            }
        }

        System.out.println("\nRoom allocation for " + n + " travel requests:");
        String ts = OutputWriter.now();
        for (int i = 0; i < n; i++) {
            FlowData.Request q = reqs.get(i);
            String pref = q.preference.isEmpty() ? "any" : q.preference;
            String hotelName;
            String status;
            if (res.assignedRight[i] >= 0) {
                hotelName = hotels.get(res.assignedRight[i]).getName();
                status = "ASSIGNED";
            } else {
                hotelName = "";
                boolean any = false;
                for (int j = 0; j < m; j++) if (ok[i][j]) any = true;
                status = any ? "NO ROOM LEFT" : "NO MATCHING HOTEL";
            }
            System.out.printf("%-10s %-12s %-10s -> %s%n", q.traveler, q.destination, pref,
                    hotelName.isEmpty() ? status : hotelName);
            OutputWriter.append("assignments.csv", "timestamp,traveler,destination,preference,hotel,status",
                    ts, q.traveler, q.destination, q.preference, hotelName, status);
        }
        System.out.println("\nAssigned with max-flow matching: " + res.matched + " of " + n);
        System.out.println("Assigned with simple first-come : " + greedy + " of " + n);
        System.out.println("(This is a plan only. Room stock is unchanged until you use 'Book a hotel'.)");
        OutputWriter.log("ALLOCATE", "Bipartite matching (Dinic)", n + " requests",
                res.matched + " assigned, first-come would give " + greedy);
    }
}