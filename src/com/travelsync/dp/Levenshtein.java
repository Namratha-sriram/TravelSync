package com.travelsync.dp;

import java.util.List;

public class Levenshtein {

    // dp[i][j] = min edits to turn first i chars of a into first j chars of b. O(m*n)
    public static int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;   // delete all
        for (int j = 0; j <= n; j++) dp[0][j] = j;   // insert all

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,        // delete
                                 dp[i][j - 1] + 1),       // insert
                        dp[i - 1][j - 1] + cost);         // substitute / match
            }
        }
        return dp[m][n];
    }

    // Suggest the closest known name for a misspelled input
    public static String closest(String input, List<String> candidates) {
        String best = null;
        int bestDist = Integer.MAX_VALUE;
        for (String c : candidates) {
            int d = distance(input, c);
            if (d < bestDist) {
                bestDist = d;
                best = c;
            }
        }
        return best;
    }
}