package com.travelsync.strings;

import java.util.ArrayList;
import java.util.List;

public class ZFunction {

    // z[i] = length of longest substring starting at i that matches a prefix of s
    public static int[] compute(String s) {
        int n = s.length();
        int[] z = new int[n];
        int l = 0, r = 0;
        for (int i = 1; i < n; i++) {
            if (i < r) z[i] = Math.min(r - i, z[i - l]);
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) z[i]++;
            if (i + z[i] > r) { l = i; r = i + z[i]; }
        }
        if (n > 0) z[0] = n;
        return z;
    }

    // Pattern search using Z on pattern + '$' + text
    public static List<Integer> search(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        if (pattern.isEmpty()) return result;
        String combined = pattern + "$" + text;
        int[] z = compute(combined);
        int m = pattern.length();
        for (int i = m + 1; i < combined.length(); i++) {
            if (z[i] == m) result.add(i - m - 1);
        }
        return result;
    }

    // Longest phrase that occurs at least twice without overlapping.
    // Runs Z-function on every suffix, so O(n^2) - fine for description-sized text.
    public static String longestRepeatedPhrase(String text) {
        String best = "";
        for (int start = 0; start < text.length(); start++) {
            String suffix = text.substring(start);

            // a phrase must not cross the '|' line separator
            int sep = suffix.indexOf('|');
            if (sep == 0) continue;
            int limit = (sep == -1) ? suffix.length() : sep;

            int[] z = compute(suffix);
            for (int j = 1; j < suffix.length(); j++) {
                int len = Math.min(z[j], j);       // no overlap between the two copies
                len = Math.min(len, limit);        // stop at the separator
                if (len > best.length()) best = suffix.substring(0, len);
            }
        }
        return best.trim();
    }
}