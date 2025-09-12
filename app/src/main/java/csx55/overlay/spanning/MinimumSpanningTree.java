package csx55.overlay.spanning;

import java.util.*;

import csx55.overlay.util.OverlayCreator;
import csx55.overlay.util.Tuple;

public class MinimumSpanningTree {

    Map<String, List<Tuple>> overlay;
    OverlayCreator oc;
    Set<Node> nodes; // make a map?
    List<Edge> edges;
    List<Edge> mst;

    public MinimumSpanningTree(Map<String, List<Tuple>> overlay, OverlayCreator oc) {
        this.overlay = overlay;
        this.nodes = new HashSet<>();
        this.edges = new ArrayList<>();
        this.mst = new ArrayList<>();
        this.oc = oc;

        // assign treeNum variable to each node with 0
        for(Map.Entry<String, List<Tuple>> entry : overlay.entrySet()){
            Node node = new Node(entry.getKey());
            nodes.add(node);
        }

        createEdges();
        generateMST(edges, mst);
    }

    public void printNodes() {
        for(Node node : nodes) {
            System.out.println(node.toString());
        }
    }
    
    // filter(overlay) -> filteredOverlay
        // sort(filtered) by weight
    Map<String, List<Tuple>> filteredOverlay;
    private void createEdges() {
        filteredOverlay = new HashMap<>(oc.filter(overlay));
        for(Map.Entry<String, List<Tuple>> entry : overlay.entrySet()){
            Edge e = new Edge(entry.getKey(), entry.getValue().get(0).getEndpoint(), entry.getValue().get(1).getWeight());
            edges.add(e);
        }
        Collections.sort(edges, Comparator.comparing(Edge::getWeight));
    }

    public void printEdges() {
        for(Edge e : edges) {
            System.out.println(e.toString());
        }
    }
    
    // algo
        // treeNum = 1
        // lowestTreeNum = treeNum
        // while(set.size() != n - 1)
            //if(set not empty and (nodeOne is null and nodeTwo is null))
                // add edge to set
                // assign treeNum to nodes
            // if(nodeOne treeNum is null or nodeTwo treeNum is null)
                // add edge to set
                // assign lowest treeNum to EVERY NODE
            // if(set not empty and (nodeOne is null and nodeTwo is null))
                // add edge to set
                // assign treeNum+1 to nodes
    private void generateMST(List<Edge> edges, List<Edge> mst) {
        int treeNum = 1;
        while(mst.size() != nodes.size() - 1) {
            for(Edge e : edges) {
                if(!mst.isEmpty() && ())
            }
        }
    }


    private class Node {
        String id;
        int treeNum;

        private Node(String id) {
            this.id = id;
            this.treeNum = 0;
        }

        public String getId() {
            return id;
        }

        public int getTreeNum() {
            return treeNum;
        }

        @Override
        public String toString() {
            return getId() + ", " + getTreeNum();
        }

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
        
        private int getWeight(){
            return weight;
        }

        @Override
        public String toString() {
            return nodeA + ", " + nodeB + ", " + weight;
        }
    }
}