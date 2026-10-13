package com.travelsync.suffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SuffixArray {
    private final String text;
    private final int n;
    private final int[] sa;     // sa[i] = starting index of the i-th smallest suffix

    // Prefix doubling: sort suffixes by first 1, 2, 4, 8... characters. O(n log^2 n)
    public SuffixArray(String text) {
        this.text = text;
        this.n = text.length();
        this.sa = new int[n];
        if (n == 0) return;

        Integer[] idx = new Integer[n];
        int[] rank = new int[n];
        int[] tmp = new int[n];
        for (int i = 0; i < n; i++) {
            idx[i] = i;
            rank[i] = text.charAt(i);
        }

        for (int k = 1; ; k <<= 1) {
            final int step = k;
            Comparator<Integer> cmp = (a, b) -> {
                if (rank[a] != rank[b]) return Integer.compare(rank[a], rank[b]);
                int ra = a + step < n ? rank[a + step] : -1;
                int rb = b + step < n ? rank[b + step] : -1;
                return Integer.compare(ra, rb);
            };
            Arrays.sort(idx, cmp);

            tmp[idx[0]] = 0;
            for (int i = 1; i < n; i++) {
                tmp[idx[i]] = tmp[idx[i - 1]] + (cmp.compare(idx[i - 1], idx[i]) < 0 ? 1 : 0);
            }
            System.arraycopy(tmp, 0, rank, 0, n);
            if (rank[idx[n - 1]] == n - 1) break;   // all ranks distinct -> done
        }
        for (int i = 0; i < n; i++) sa[i] = idx[i];
    }

    public int[] getSA() { return sa; }
    public String getText() { return text; }

    // compare the suffix at pos with the pattern (only the first m characters)
    private int compareSuffix(int pos, String p) {
        String suf = text.substring(pos, Math.min(n, pos + p.length()));
        return suf.compareTo(p);
    }

    // Binary search for the first suffix >= pattern, then collect matches. O(m log n + k)
    public List<Integer> search(String pattern) {
        List<Integer> result = new ArrayList<>();
        if (pattern.isEmpty() || n == 0) return result;

        int lo = 0, hi = n;
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (compareSuffix(sa[mid], pattern) < 0) lo = mid + 1;
            else hi = mid;
        }
        while (lo < n && compareSuffix(sa[lo], pattern) == 0) {
            result.add(sa[lo]);
            lo++;
        }
        return result;
    }

    public void print(int limit) {
        for (int i = 0; i < Math.min(limit, n); i++) {
            String s = text.substring(sa[i], Math.min(n, sa[i] + 25));
            System.out.printf("%3d  sa=%3d  %s%n", i, sa[i], s);
        }
    }
}