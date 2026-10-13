package com.travelsync.dp;

public class OptimalBST {
    private final String[] keys;   // must be in sorted order
    private final int[][] cost;
    private final int[][] root;
    private final int n;

    // cost[i][j] = min weighted search cost for keys i..j (1-indexed). Time O(n^3), space O(n^2)
    public OptimalBST(String[] keys, int[] freq) {
        this.keys = keys;
        this.n = keys.length;
        cost = new int[n + 2][n + 2];
        root = new int[n + 2][n + 2];

        int[] prefix = new int[n + 1];
        for (int i = 1; i <= n; i++) prefix[i] = prefix[i - 1] + freq[i - 1];

        for (int i = 1; i <= n; i++) {
            cost[i][i] = freq[i - 1];
            root[i][i] = i;
        }

        for (int len = 2; len <= n; len++) {
            for (int i = 1; i + len - 1 <= n; i++) {
                int j = i + len - 1;
                cost[i][j] = Integer.MAX_VALUE;
                int sum = prefix[j] - prefix[i - 1];
                for (int r = i; r <= j; r++) {
                    int left = r > i ? cost[i][r - 1] : 0;
                    int right = r < j ? cost[r + 1][j] : 0;
                    int c = left + right + sum;      // every key sinks one level under the root
                    if (c < cost[i][j]) {
                        cost[i][j] = c;
                        root[i][j] = r;
                    }
                }
            }
        }
    }

    public int minCost() { return cost[1][n]; }

    public String structure() {
        StringBuilder sb = new StringBuilder();
        print(1, n, "", "Root: ", sb);
        return sb.toString();
    }

    private void print(int i, int j, String indent, String label, StringBuilder sb) {
        if (i > j) return;
        int r = root[i][j];
        sb.append(indent).append(label).append(keys[r - 1]).append("\n");
        print(i, r - 1, indent + "    ", "L: ", sb);
        print(r + 1, j, indent + "    ", "R: ", sb);
    }
}