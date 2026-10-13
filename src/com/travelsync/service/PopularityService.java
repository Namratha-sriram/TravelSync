package com.travelsync.service;

import com.travelsync.dp.OptimalBST;
import com.travelsync.model.Booking;
import com.travelsync.model.Destination;
import com.travelsync.data.OutputWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PopularityService {
    // TreeMap keeps destination names sorted, which the Optimal BST needs
    private final Map<String, Integer> counts = new TreeMap<>();

    public PopularityService(List<Destination> destinations, List<Booking> bookings) {
        for (Destination d : destinations) counts.put(d.getName(), 1);
        for (Booking b : bookings) counts.merge(b.getDestination(), 1, Integer::sum);
    }

    // called on every search, view and booking
    public void record(String destination) {
        counts.merge(destination, 1, Integer::sum);
    }

    public void show() {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(counts.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        System.out.println("Trending destinations (searches + bookings):");
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            System.out.printf(" %d. %-14s score %d%n", i + 1, list.get(i).getKey(), list.get(i).getValue());
            OutputWriter.append("trending.csv", "timestamp,rank,destination,score", OutputWriter.now(),
                    String.valueOf(i + 1), list.get(i).getKey(), String.valueOf(list.get(i).getValue()));
        }
        OutputWriter.log("TRENDING", "Popularity score", "searches plus bookings", Math.min(5, list.size()) + " results exported to trending.csv");

        String[] keys = counts.keySet().toArray(new String[0]);
        if (keys.length == 0) return;
        int[] freq = new int[keys.length];
        for (int i = 0; i < keys.length; i++) freq[i] = counts.get(keys[i]);

        OptimalBST bst = new OptimalBST(keys, freq);
        long balanced = balancedCost(freq, 0, freq.length - 1, 1);

        System.out.println("\nFast-lookup index built from these scores (Optimal BST):");
        System.out.print(bst.structure());
        System.out.println("Optimal BST weighted lookup cost : " + bst.minCost());
        System.out.println("Plain balanced BST lookup cost   : " + balanced);
    }

    // cost of a plain balanced BST, for comparison
    private static long balancedCost(int[] f, int lo, int hi, int depth) {
        if (lo > hi) return 0;
        int mid = (lo + hi) / 2;
        return (long) f[mid] * depth
                + balancedCost(f, lo, mid - 1, depth + 1)
                + balancedCost(f, mid + 1, hi, depth + 1);
    }
}
