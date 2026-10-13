package com.travelsync.flow;

public class FlowResult {
    public final int flow;     // maximum flow value
    public final int steps;    // augmenting paths (Ford-Fulkerson, Edmonds-Karp) or phases (Dinic)

    public FlowResult(int flow, int steps) {
        this.flow = flow;
        this.steps = steps;
    }
}