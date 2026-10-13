package com.travelsync.strings;

import java.util.ArrayList;
import java.util.List;

public class KMP {

    // LPS = longest proper prefix which is also a suffix
    public static int[] buildLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0, i = 1;
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i++] = ++len;
            } else if (len > 0) {
                len = lps[len - 1];   // fall back, don't advance i
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    // Returns all starting indices where pattern occurs in text
    public static List<Integer> search(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0 || m > n) return result;

        int[] lps = buildLPS(pattern);
        int i = 0, j = 0;
        while (i < n) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++; j++;
                if (j == m) {
                    result.add(i - m);
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return result;
    }

    // Case-insensitive helper for user searches
    public static List<Integer> searchIgnoreCase(String text, String pattern) {
        return search(text.toLowerCase(), pattern.toLowerCase());
    }

    // Naive search, used only for comparison in Benchmark
    public static List<Integer> naiveSearch(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        for (int i = 0; i + m <= n; i++) {
            int j = 0;
            while (j < m && text.charAt(i + j) == pattern.charAt(j)) j++;
            if (j == m) result.add(i);
        }
        return result;
    }
}