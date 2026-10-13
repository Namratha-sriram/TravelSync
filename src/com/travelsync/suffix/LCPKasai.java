package com.travelsync.suffix;

public class LCPKasai {

    // lcp[i] = longest common prefix of suffixes sa[i-1] and sa[i]. O(n)
    public static int[] build(String s, int[] sa) {
        int n = s.length();
        int[] rank = new int[n];
        int[] lcp = new int[n];
        for (int i = 0; i < n; i++) rank[sa[i]] = i;

        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1];
                while (i + h < n && j + h < n && s.charAt(i + h) == s.charAt(j + h)) h++;
                lcp[rank[i]] = h;
                if (h > 0) h--;      // key trick: LCP drops by at most 1
            } else {
                h = 0;
            }
        }
        return lcp;
    }

    // Longest common substring of two descriptions using SA + LCP on a + '#' + b
    public static String longestCommonSubstring(String a, String b) {
        String s = a + "#" + b;
        SuffixArray sarr = new SuffixArray(s);
        int[] sa = sarr.getSA();
        int[] lcp = build(s, sa);

        int best = 0, pos = 0;
        for (int i = 1; i < s.length(); i++) {
            boolean firstInA = sa[i - 1] < a.length();
            boolean secondInA = sa[i] < a.length();
            if (firstInA != secondInA && lcp[i] > best) {   // neighbours from different strings
                best = lcp[i];
                pos = sa[i];
            }
        }
        return s.substring(pos, pos + best).trim();
    }
}