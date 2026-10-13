package com.travelsync.flow;

// Bipartite matching as max flow: source -> left (1) -> right (1, if eligible) -> sink (right capacity)
public class BipartiteMatching {

    public static class Result {
        public final int matched;
        public final int[] assignedRight;   // assignedRight[l] = right node given to l, or -1

        Result(int matched, int[] assignedRight) {
            this.matched = matched;
            this.assignedRight = assignedRight;
        }
    }

    public static Result assign(boolean[][] eligible, int[] rightCap) {
        int left = eligible.length;
        int right = rightCap.length;
        int source = left + right;
        int sink = source + 1;

        int edges = left + right;
        for (int l = 0; l < left; l++)
            for (int r = 0; r < right; r++)
                if (eligible[l][r]) edges++;

        FlowGraph g = new FlowGraph(left + right + 2, edges);
        for (int l = 0; l < left; l++) g.addEdge(source, l, 1);
        for (int l = 0; l < left; l++)
            for (int r = 0; r < right; r++)
                if (eligible[l][r]) g.addEdge(l, left + r, 1);
        for (int r = 0; r < right; r++) g.addEdge(left + r, sink, Math.max(0, rightCap[r]));

        FlowResult fr = Dinic.maxFlow(g, source, sink);

        int[] assigned = new int[left];
        for (int l = 0; l < left; l++) assigned[l] = -1;
        for (int e = 0; e < g.edgeCount(); e += 2) {
            int from = g.edgeFrom(e), to = g.edgeTo(e);
            if (from < left && to >= left && to < left + right && g.residualCapacity(e) == 0) {
                assigned[from] = to - left;     // this unit edge carries flow
            }
        }
        return new BipartiteMatching.Result(fr.flow, assigned);
    }
}