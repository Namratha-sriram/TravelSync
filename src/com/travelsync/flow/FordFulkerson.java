package com.travelsync.flow;

// Ford-Fulkerson with DFS augmenting paths. Time O(E * maxFlow) for integer capacities.
public class FordFulkerson {

    public static FlowResult maxFlow(FlowGraph g, int s, int t) {
        g.reset();
        if (s == t) return new FlowResult(0, 0);
        int flow = 0, paths = 0;
        while (true) {
            boolean[] visited = new boolean[g.n];
            int pushed = dfs(g, s, t, Integer.MAX_VALUE, visited);
            if (pushed == 0) break;
            flow += pushed;
            paths++;
        }
        return new FlowResult(flow, paths);
    }

    private static int dfs(FlowGraph g, int u, int t, int limit, boolean[] visited) {
        if (u == t) return limit;
        visited[u] = true;
        for (int e = g.head[u]; e != -1; e = g.next[e]) {
            int v = g.to[e];
            if (!visited[v] && g.cap[e] > 0) {
                int d = dfs(g, v, t, Math.min(limit, g.cap[e]), visited);
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