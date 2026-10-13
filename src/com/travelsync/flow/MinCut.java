package com.travelsync.flow;

// Min-cut from the residual graph after a max flow: the edges leaving the set reachable from s
public class MinCut {
    public final int capacity;         // equals the max flow (max-flow min-cut theorem)
    public final int[] cutEdges;       // indices of the forward edges that form the cut
    public final boolean[] sourceSide; // true for nodes still reachable from the source

    private MinCut(int capacity, int[] cutEdges, boolean[] sourceSide) {
        this.capacity = capacity;
        this.cutEdges = cutEdges;
        this.sourceSide = sourceSide;
    }

    public static MinCut compute(FlowGraph g, int s, int t) {
        FlowResult r = Dinic.maxFlow(g, s, t);      // leaves the residual capacities in g

        boolean[] reach = new boolean[g.n];
        int[] queue = new int[g.n];
        int qh = 0, qt = 0;
        queue[qt++] = s;
        reach[s] = true;
        while (qh < qt) {
            int u = queue[qh++];
            for (int e = g.head[u]; e != -1; e = g.next[e]) {
                int v = g.to[e];
                if (!reach[v] && g.cap[e] > 0) {
                    reach[v] = true;
                    queue[qt++] = v;
                }
            }
        }

        int count = 0;
        for (int e = 0; e < g.edgeCount; e += 2) {
            if (reach[g.edgeFrom(e)] && !reach[g.to[e]] && g.orig[e] > 0) count++;
        }
        int[] cut = new int[count];
        int k = 0;
        for (int e = 0; e < g.edgeCount; e += 2) {
            if (reach[g.edgeFrom(e)] && !reach[g.to[e]] && g.orig[e] > 0) cut[k++] = e;
        }
        return new MinCut(r.flow, cut, reach);
    }
}