package com.travelsync.flow;

// Directed graph with capacities, stored in plain arrays (edge i and i^1 are a forward/backward pair)
public class FlowGraph {
    final int n;
    final int[] head;
    final int[] to;
    final int[] next;
    final int[] cap;      // residual capacity (changes while a flow algorithm runs)
    final int[] orig;     // original capacity
    int edgeCount = 0;

    public FlowGraph(int n, int maxEdges) {
        this.n = n;
        head = new int[n];
        for (int i = 0; i < n; i++) head[i] = -1;
        to = new int[2 * maxEdges];
        next = new int[2 * maxEdges];
        cap = new int[2 * maxEdges];
        orig = new int[2 * maxEdges];
    }

    public void addEdge(int u, int v, int c) {
        to[edgeCount] = v;
        cap[edgeCount] = c;
        orig[edgeCount] = c;
        next[edgeCount] = head[u];
        head[u] = edgeCount++;

        to[edgeCount] = u;          // backward edge with zero capacity
        cap[edgeCount] = 0;
        orig[edgeCount] = 0;
        next[edgeCount] = head[v];
        head[v] = edgeCount++;
    }

    // restore all capacities so another algorithm can run on the same graph
    public void reset() {
        for (int i = 0; i < edgeCount; i++) cap[i] = orig[i];
    }

    public int size() { return n; }
    public int edgeCount() { return edgeCount; }
    public boolean isForward(int e) { return (e & 1) == 0; }
    public int edgeFrom(int e) { return to[e ^ 1]; }
    public int edgeTo(int e) { return to[e]; }
    public int originalCapacity(int e) { return orig[e]; }
    public int residualCapacity(int e) { return cap[e]; }
}