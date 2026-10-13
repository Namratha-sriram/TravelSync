package com.travelsync.dp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItineraryBitmaskDP {

    public static class Result {
        public int cost;
        public List<Integer> path;
    }

    // Cheapest route that starts at 'start' and visits every destination once.
    // dp[mask][last] = min cost having visited the set 'mask', currently at 'last'.
    // Time O(2^n * n^2), space O(2^n * n). Works for small n (about 15-20).
    public static Result shortestPath(int[][] d, int start) {
        int n = d.length;
        int full = 1 << n;
        int INF = Integer.MAX_VALUE / 2;

        int[][] dp = new int[full][n];
        int[][] parent = new int[full][n];
        for (int i = 0; i < full; i++) {
            for (int j = 0; j < n; j++) {
                dp[i][j] = INF;
                parent[i][j] = -1;
            }
        }
        dp[1 << start][start] = 0;

        for (int mask = 0; mask < full; mask++) {
            for (int last = 0; last < n; last++) {
                if ((mask & (1 << last)) == 0 || dp[mask][last] >= INF) continue;
                for (int nxt = 0; nxt < n; nxt++) {
                    if ((mask & (1 << nxt)) != 0) continue;         // already visited
                    int newMask = mask | (1 << nxt);
                    int c = dp[mask][last] + d[last][nxt];
                    if (c < dp[newMask][nxt]) {
                        dp[newMask][nxt] = c;
                        parent[newMask][nxt] = last;
                    }
                }
            }
        }

        int bestLast = -1, best = INF;
        for (int last = 0; last < n; last++) {
            if (dp[full - 1][last] < best) {
                best = dp[full - 1][last];
                bestLast = last;
            }
        }

        // rebuild the path by walking parents backwards
        List<Integer> path = new ArrayList<>();
        int mask = full - 1, cur = bestLast;
        while (cur != -1) {
            path.add(cur);
            int prev = parent[mask][cur];
            mask ^= (1 << cur);
            cur = prev;
        }
        Collections.reverse(path);

        Result r = new Result();
        r.cost = best;
        r.path = path;
        return r;
    }
}