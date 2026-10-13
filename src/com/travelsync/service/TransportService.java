package com.travelsync.service;

import com.travelsync.data.FlowData;
import com.travelsync.flow.FlowGraph;

import java.util.ArrayList;
import java.util.List;

// Turns the routes from transport.csv into a flow graph
public class TransportService {
    private final List<FlowData.Route> routes;
    private final List<String> cities = new ArrayList<>();
    private final FlowGraph graph;

    public TransportService(List<FlowData.Route> routes) {
        this.routes = routes;
        for (FlowData.Route r : routes) {
            addCity(r.from);
            addCity(r.to);
        }
        graph = new FlowGraph(cities.size(), routes.size());
        for (FlowData.Route r : routes) {
            graph.addEdge(index(r.from), index(r.to), r.capacity);   // edge 2*i belongs to routes.get(i)
        }
    }

    private void addCity(String name) {
        if (index(name) < 0) cities.add(name);
    }

    public final int index(String city) {
        for (int i = 0; i < cities.size(); i++) {
            if (cities.get(i).equalsIgnoreCase(city)) return i;
        }
        return -1;
    }

    public List<String> cities() { return cities; }
    public List<FlowData.Route> routes() { return routes; }
    public FlowGraph graph() { return graph; }
}