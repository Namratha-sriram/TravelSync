package com.travelsync.main;

import com.travelsync.analysis.Benchmark;
import com.travelsync.dp.*;
import com.travelsync.model.Booking;
import com.travelsync.model.Destination;
import com.travelsync.strings.*;
import com.travelsync.suffix.*;

import java.util.*;

public class AdminPanel {
    private final Scanner sc;
    private final List<Booking> bookings;
    private final List<String> descriptions;
    private final List<String> names = new ArrayList<>();
    private final String joined;

    public AdminPanel(Scanner sc, List<Destination> destinations,
                      List<Booking> bookings, List<String> descriptions) {
        this.sc = sc;
        this.bookings = bookings;
        this.descriptions = descriptions;
        for (Destination d : destinations) names.add(d.getName());
        this.joined = String.join("|", descriptions);
    }

    public void run() {
        while (true) {
            System.out.println("\n----- Admin Panel: Algorithm Lab -----");
            System.out.println(" 1. Keyword search in descriptions   (KMP)");
            System.out.println(" 2. Booking code search              (Rabin-Karp)");
            System.out.println(" 3. Repeated phrase in descriptions  (Z-Function)");
            System.out.println(" 4. Tag descriptions by keywords     (Aho-Corasick)");
            System.out.println(" 5. Substring search across index    (Suffix Array)");
            System.out.println(" 6. Most similar descriptions        (LCP / Kasai)");
            System.out.println(" 7. Substring exists?                (Suffix Automaton)");
            System.out.println(" 8. Correct a destination name       (Levenshtein / Damerau)");
            System.out.println(" 9. Best itinerary demo              (Bitmask DP)");
            System.out.println("10. Matrix-Chain and Optimal BST");
            System.out.println("11. Complexity benchmark");
            System.out.println(" 0. Back");
            System.out.print("Choice: ");

            int ch;
            try {
                ch = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a number.");
                continue;
            }
            if (ch == 0) return;

            switch (ch) {
                case 1 -> kmp();
                case 2 -> rabinKarp();
                case 3 -> zFunction();
                case 4 -> ahoCorasick();
                case 5 -> suffixArray();
                case 6 -> similarity();
                case 7 -> automaton();
                case 8 -> nameCorrection();
                case 9 -> itinerary();
                case 10 -> matrixAndBst();
                case 11 -> Benchmark.run();
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private int lineOf(int pos) {
        int line = 0;
        for (int i = 0; i < pos; i++) if (joined.charAt(i) == '|') line++;
        return line;
    }

    private void kmp() {
        String q = ask("Keyword: ");
        boolean found = false;
        for (int i = 0; i < descriptions.size(); i++) {
            if (!KMP.searchIgnoreCase(descriptions.get(i), q).isEmpty()) {
                System.out.println("Line " + i + ": " + descriptions.get(i));
                found = true;
            }
        }
        if (!found) System.out.println("No match.");
    }

    private void rabinKarp() {
        String code = ask("Booking code (e.g. BK1004): ");
        boolean found = false;
        for (Booking b : bookings) {
            if (!RabinKarp.search(b.toString(), code).isEmpty()) {
                System.out.println("Found: " + b);
                found = true;
            }
        }
        if (!found) System.out.println("No booking found.");
    }

    private void zFunction() {
        System.out.println("Longest repeated phrase: \"" + ZFunction.longestRepeatedPhrase(joined) + "\"");
    }

    private void ahoCorasick() {
        AhoCorasick ac = new AhoCorasick(Arrays.asList(
                "beach", "snow", "temple", "luxury", "seafood", "nightlife", "mountain", "palace"));
        for (int i = 0; i < descriptions.size(); i++)
            System.out.println("Line " + i + " -> " + ac.distinctKeywords(descriptions.get(i)));
    }

    private void suffixArray() {
        String q = ask("Substring: ");
        SuffixArray sa = new SuffixArray(joined);
        List<Integer> hits = sa.search(q);
        if (hits.isEmpty()) System.out.println("No match.");
        for (int pos : hits)
            System.out.println("Position " + pos + " (description line " + lineOf(pos) + ")");
    }

    private void similarity() {
        int bestLen = 0, bi = 0, bj = 0;
        String best = "";
        for (int i = 0; i < descriptions.size(); i++) {
            for (int j = i + 1; j < descriptions.size(); j++) {
                String c = LCPKasai.longestCommonSubstring(descriptions.get(i), descriptions.get(j));
                if (c.length() > bestLen) {
                    bestLen = c.length();
                    best = c;
                    bi = i;
                    bj = j;
                }
            }
        }
        System.out.println("Most similar: line " + bi + " and line " + bj);
        System.out.println("Common part: \"" + best + "\"");
    }

    private void automaton() {
        SuffixAutomaton sam = new SuffixAutomaton(joined);
        String q = ask("Substring: ");
        System.out.println("Present: " + sam.contains(q));
        System.out.println("Distinct substrings in index: " + sam.countDistinctSubstrings());
    }

    private void nameCorrection() {
        String q = ask("Type a destination (typos allowed): ");
        String lev = Levenshtein.closest(q, names);
        String dam = DamerauLevenshtein.closest(q, names);
        if (lev == null || dam == null) {
            System.out.println("No destinations loaded.");
            return;
        }
        System.out.println("Levenshtein: " + lev + " (distance " + Levenshtein.distance(q, lev) + ")");
        System.out.println("Damerau:     " + dam + " (distance " + DamerauLevenshtein.distance(q, dam) + ")");
    }

    private void itinerary() {
        String[] city = {"Delhi", "Jaipur", "Manali", "Goa", "Kerala"};
        int[][] d = {
            { 0,  5, 12, 26, 45},
            { 5,  0, 16, 22, 40},
            {12, 16,  0, 38, 55},
            {26, 22, 38,  0, 12},
            {45, 40, 55, 12,  0}
        };
        ItineraryBitmaskDP.Result r = ItineraryBitmaskDP.shortestPath(d, 0);
        StringBuilder sb = new StringBuilder();
        for (int c : r.path) sb.append(city[c]).append(" -> ");
        System.out.println("Best route: " + sb.substring(0, sb.length() - 4));
        System.out.println("Total travel time: " + r.cost + " hours");
    }

    private void matrixAndBst() {
        MatrixChain mc = new MatrixChain(new int[]{40, 20, 30, 10, 30});
        System.out.println("Matrix chain min cost: " + mc.minCost() + "  order: " + mc.parenthesization());
        OptimalBST bst = new OptimalBST(new String[]{"Goa", "Jaipur", "Kerala"}, new int[]{34, 8, 50});
        System.out.println("Optimal BST cost: " + bst.minCost());
        System.out.print(bst.structure());
    }
}
