package com.travelsync.suffix;

import java.util.Arrays;

public class SuffixAutomaton {
    private static final int ALPHA = 128;   // ASCII
    private final int[] len;
    private final int[] link;
    private final int[][] next;
    private int size, last;

    public SuffixAutomaton(String s) {
        int max = 2 * s.length() + 2;
        len = new int[max];
        link = new int[max];
        next = new int[max][ALPHA];
        for (int[] row : next) Arrays.fill(row, -1);
        link[0] = -1;
        size = 1;
        last = 0;
        for (char c : s.toCharArray()) extend(idx(c));
    }

    private int idx(char c) { return c < ALPHA ? c : ALPHA - 1; }

    private void extend(int c) {
        int cur = size++;
        len[cur] = len[last] + 1;
        int p = last;
        while (p != -1 && next[p][c] == -1) {
            next[p][c] = cur;
            p = link[p];
        }
        if (p == -1) {
            link[cur] = 0;
        } else {
            int q = next[p][c];
            if (len[p] + 1 == len[q]) {
                link[cur] = q;
            } else {
                int clone = size++;
                len[clone] = len[p] + 1;
                next[clone] = next[q].clone();
                link[clone] = link[q];
                while (p != -1 && next[p][c] == q) {
                    next[p][c] = clone;
                    p = link[p];
                }
                link[q] = clone;
                link[cur] = clone;
            }
        }
        last = cur;
    }

    // Is 'pattern' a substring? O(m)
    public boolean contains(String pattern) {
        int state = 0;
        for (char ch : pattern.toCharArray()) {
            state = next[state][idx(ch)];
            if (state == -1) return false;
        }
        return true;
    }

    // Number of distinct substrings = sum of (len[v] - len[link[v]])
    public long countDistinctSubstrings() {
        long total = 0;
        for (int v = 1; v < size; v++) total += len[v] - len[link[v]];
        return total;
    }

    public int getStateCount() { return size; }
}