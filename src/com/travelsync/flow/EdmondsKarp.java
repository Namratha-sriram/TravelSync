package com.travelsync.flow;

// Edmonds-Karp: Ford-Fulkerson where every augmenting path is the shortest one (BFS). Time O(V * E^2).
public class EdmondsKarp {

    public static FlowResult maxFlow(FlowGraph g, int s, int t) {
        g.reset();
        if (s == t) return new FlowResult(0, 0);
        int flow = 0, paths = 0;
        int[] prevEdge = new int[g.n];
        int[] queue = new int[g.n];

        while (true) {
            boolean[] seen = new boolean[g.n];
            for (int i = 0; i < g.n; i++) prevEdge[i] = -1;
            int qh = 0, qt = 0;
            queue[qt++] = s;
            seen[s] = true;
            while (qh < qt && !seen[t]) {
                int u = queue[qh++];
                for (int e = g.head[u]; e != -1; e = g.next[e]) {
                    int v = g.to[e];
                    if (!seen[v] && g.cap[e] > 0) {
                        seen[v] = true;
                        prevEdge[v] = e;
                        queue[qt++] = v;
                    }
                }
            }
            if (!seen[t]) break;

            int bottleneck = Integer.MAX_VALUE;
            for (int v = t; v != s; v = g.to[prevEdge[v] ^ 1]) {
                bottleneck = Math.min(bottleneck, g.cap[prevEdge[v]]);
            }
            for (int v = t; v != s; v = g.to[prevEdge[v] ^ 1]) {
                g.cap[prevEdge[v]] -= bottleneck;
                g.cap[prevEdge[v] ^ 1] += bottleneck;
            }
            flow += bottleneck;
            paths++;
        }
        return new FlowResult(flow, paths);
    }
}