package com.travelsync.flow;

// Dinic: BFS builds a level graph, DFS sends a blocking flow, repeat. Time O(V^2 * E).
public class Dinic {

    public static FlowResult maxFlow(FlowGraph g, int s, int t) {
        g.reset();
        if (s == t) return new FlowResult(0, 0);
        int flow = 0, phases = 0;
        int[] level = new int[g.n];
        int[] it = new int[g.n];

        while (buildLevels(g, s, t, level)) {
            for (int i = 0; i < g.n; i++) it[i] = g.head[i];
            int pushed;
            while ((pushed = dfs(g, s, t, Integer.MAX_VALUE, level, it)) > 0) flow += pushed;
            phases++;
        }
        return new FlowResult(flow, phases);
    }

    private static boolean buildLevels(FlowGraph g, int s, int t, int[] level) {
        for (int i = 0; i < g.n; i++) level[i] = -1;
        int[] queue = new int[g.n];
        int qh = 0, qt = 0;
        queue[qt++] = s;
        level[s] = 0;
        while (qh < qt) {
            int u = queue[qh++];
            for (int e = g.head[u]; e != -1; e = g.next[e]) {
                int v = g.to[e];
                if (level[v] < 0 && g.cap[e] > 0) {
                    level[v] = level[u] + 1;
                    queue[qt++] = v;
                }
            }
        }
        return level[t] >= 0;
    }

    private static int dfs(FlowGraph g, int u, int t, int limit, int[] level, int[] it) {
        if (u == t) return limit;
        for (; it[u] != -1; it[u] = g.next[it[u]]) {
            int e = it[u];
            int v = g.to[e];
            if (g.cap[e] > 0 && level[v] == level[u] + 1) {
                int d = dfs(g, v, t, Math.min(limit, g.cap[e]), level, it);
                if (d > 0) {
                    g.cap[e] -= d;
                    g.cap[e ^ 1] += d;
                    return d;
                }
            }
        }
        return 0;
    }
}