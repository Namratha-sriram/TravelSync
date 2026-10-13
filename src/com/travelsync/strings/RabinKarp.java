package com.travelsync.strings;

import java.util.ArrayList;
import java.util.List;

public class RabinKarp {
    private static final long BASE = 256;
    private static final long MOD = 1_000_000_007L;

    public static List<Integer> search(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0 || m > n) return result;

        // highest power = BASE^(m-1) % MOD
        long high = 1;
        for (int i = 1; i < m; i++) high = (high * BASE) % MOD;

        long pHash = 0, tHash = 0;
        for (int i = 0; i < m; i++) {
            pHash = (pHash * BASE + pattern.charAt(i)) % MOD;
            tHash = (tHash * BASE + text.charAt(i)) % MOD;
        }

        for (int i = 0; i + m <= n; i++) {
            // hash match -> verify to rule out collisions
            if (pHash == tHash && text.regionMatches(i, pattern, 0, m)) {
                result.add(i);
            }
            if (i + m < n) {
                // rolling hash: remove text[i], add text[i+m]
                tHash = (tHash - text.charAt(i) * high % MOD + MOD) % MOD;
                tHash = (tHash * BASE + text.charAt(i + m)) % MOD;
            }
        }
        return result;
    }

    // Search a booking code across all lines, e.g. "BK1004"
    public static List<Integer> findLinesContaining(List<String> lines, String code) {
        List<Integer> lineNumbers = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (!search(lines.get(i), code).isEmpty()) lineNumbers.add(i);
        }
        return lineNumbers;
    }
}