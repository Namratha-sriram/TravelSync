package com.travelsync.dp;

public class MatrixChain {
    private final long[][] m;   // m[i][j] = min multiplications for matrices i..j
    private final int[][] s;    // s[i][j] = best split point
    private final int n;

    // Matrix i has size dims[i] x dims[i+1]. Time O(n^3), space O(n^2)
    public MatrixChain(int[] dims) {
        n = dims.length - 1;
        m = new long[n][n];
        s = new int[n][n];

        for (int len = 2; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;
                m[i][j] = Long.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    long c = m[i][k] + m[k + 1][j] + (long) dims[i] * dims[k + 1] * dims[j + 1];
                    if (c < m[i][j]) {
                        m[i][j] = c;
                        s[i][j] = k;
                    }
                }
            }
        }
    }

    public long minCost() { return m[0][n - 1]; }

    public String parenthesization() { return build(0, n - 1); }

    private String build(int i, int j) {
        if (i == j) return "A" + (i + 1);
        return "(" + build(i, s[i][j]) + build(s[i][j] + 1, j) + ")";
    }
}