package csx55.overlay.spanning;

import java.util.*;

public class MinimumSpanningTree {
    
    List<Edge> mst;
    Map<String, Boolean> inTree;

    public MinimumSpanningTree(List<String> registeredNodes) {
        this.mst = new ArrayList<>();
    }

    private class Edge {

        String nodeA;
        String nodeB;
        int weight;

        private Edge(String nodeA, String nodeB, int weight) {
            this.nodeA = nodeA;
            this.nodeB = nodeB;
            this.weight = weight;
        }
    }
    
}