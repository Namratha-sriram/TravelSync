package com.travelsync.analysis;

import com.travelsync.data.OutputWriter;

import com.travelsync.dp.DamerauLevenshtein;
import com.travelsync.dp.ItineraryBitmaskDP;
import com.travelsync.dp.Levenshtein;
import com.travelsync.strings.KMP;
import com.travelsync.strings.RabinKarp;
import com.travelsync.strings.ZFunction;
import com.travelsync.suffix.SuffixArray;

import java.util.Random;

public class Benchmark {

    private static String randomText(int n, int alphabet, long seed) {
        Random r = new Random(seed);
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append((char) ('a' + r.nextInt(alphabet)));
        return sb.toString();
    }

    private static double ms(long start) {
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    public static void run() {
        long started = System.nanoTime();
        stringSearch();
        editDistance();
        suffixArrayGrowth();
        bitmaskGrowth();
        OutputWriter.append("benchmark_runs.csv", "timestamp,suite,totalMicros", OutputWriter.now(),
                "M1-M3 algorithm benchmark", String.format("%.1f", (System.nanoTime() - started) / 1000.0));
        OutputWriter.log("BENCHMARK", "M1-M3 suite", "built-in benchmark", "results exported to benchmark_runs.csv");
    }

    // 1. Naive vs KMP vs Rabin-Karp vs Z on random text and on a worst case for naive
    private static void stringSearch() {
        System.out.println("=== String search (times in ms) ===");
        System.out.printf("%-22s %8s %8s %8s %8s%n", "Case", "Naive", "KMP", "Rabin", "Z");

        String[] names = {"Random n=200k", "Worst case n=200k"};
        String[] texts = {randomText(200_000, 4, 1), "a".repeat(200_000)};
        String[] pats = {"abcabd", "a".repeat(2000) + "b"};   // worst case: long near-match

        for (int c = 0; c < 2; c++) {
            String t = texts[c], p = pats[c];
            long s = System.nanoTime();
            KMP.naiveSearch(t, p);
            double a = ms(s);
            s = System.nanoTime();
            KMP.search(t, p);
            double b = ms(s);
            s = System.nanoTime();
            RabinKarp.search(t, p);
            double d = ms(s);
            s = System.nanoTime();
            ZFunction.search(t, p);
            double e = ms(s);
            System.out.printf("%-22s %8.2f %8.2f %8.2f %8.2f%n", names[c], a, b, d, e);
        }
    }

    // 2. Levenshtein vs Damerau
    private static void editDistance() {
        System.out.println("\n=== Edit distance (times in ms) ===");
        System.out.printf("%-12s %12s %12s%n", "Length", "Levenshtein", "Damerau");
        for (int len : new int[]{500, 1000, 2000}) {
            String a = randomText(len, 4, 11), b = randomText(len, 4, 22);
            long s = System.nanoTime();
            Levenshtein.distance(a, b);
            double x = ms(s);
            s = System.nanoTime();
            DamerauLevenshtein.distance(a, b);
            double y = ms(s);
            System.out.printf("%-12d %12.2f %12.2f%n", len, x, y);
        }
    }

    // 3. Suffix array build time as n grows
    private static void suffixArrayGrowth() {
        System.out.println("\n=== Suffix array build (times in ms) ===");
        for (int n : new int[]{5_000, 10_000, 20_000, 40_000}) {
            String t = randomText(n, 4, 5);
            long s = System.nanoTime();
            new SuffixArray(t);
            System.out.printf("n=%-8d %8.2f%n", n, ms(s));
        }
    }

    // 4. Bitmask DP growth: time roughly doubles (or more) for each extra destination
    private static void bitmaskGrowth() {
        System.out.println("\n=== Bitmask DP itinerary (times in ms) ===");
        Random r = new Random(7);
        for (int n : new int[]{8, 10, 12, 14}) {
            int[][] d = new int[n][n];
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    d[i][j] = i == j ? 0 : 1 + r.nextInt(50);
            long s = System.nanoTime();
            ItineraryBitmaskDP.shortestPath(d, 0);
            System.out.printf("n=%-4d %8.2f%n", n, ms(s));
        }
    }
}
