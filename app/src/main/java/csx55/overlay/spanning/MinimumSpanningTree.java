package csx55.overlay.spanning;

import java.util.*;
import csx55.overlay.util.OverlayCreator;
import csx55.overlay.util.Tuple;
import java.util.logging.*;

public class MinimumSpanningTree {

    private Logger LOG = Logger.getLogger(MinimumSpanningTree.class.getName());

    Map<String, List<Tuple>> overlay;
    OverlayCreator oc;
    Map<String, Integer> nodes; // make a map?
    List<Edge> edges;
    List<Edge> mst;

    public MinimumSpanningTree(Map<String, List<Tuple>> overlay, OverlayCreator oc) {
        this.overlay = overlay;
        this.nodes = new HashMap<>();
        this.edges = new ArrayList<>();
        this.mst = new ArrayList<>();
        this.oc = oc;

        for(Map.Entry<String, List<Tuple>> entry : overlay.entrySet()){
            nodes.put(entry.getKey(), 0);
        }

        createEdges();
        generateMST(edges, mst);
    }

    public void printNodes() {
        for(Map.Entry<String, Integer> entry : nodes.entrySet()) {
            LOG.info(entry.getKey() + ", " + entry.getValue());
        }
    }

    Map<String, List<Tuple>> filteredOverlay;
    private void createEdges() {
        filteredOverlay = new HashMap<>(oc.filter(overlay));
        for(Map.Entry<String, List<Tuple>> entry : filteredOverlay.entrySet()){
            for(Tuple t : entry.getValue()) {
                Edge e = new Edge(entry.getKey(), t.getEndpoint(), t.getWeight());
                edges.add(e);
            }
        }
    }

    public void printFilteredOverlay() {
        for(Map.Entry<String, List<Tuple>> entry : filteredOverlay.entrySet()){
            LOG.info(entry.toString());
        }
    }

    public void printEdges() {
        for(Edge e : edges) {
            LOG.info(e.toString());
        }
    }

    private void generateMST(List<Edge> edges, List<Edge> mst) {
        Collections.sort(edges, Comparator.comparing(Edge::getWeight)); // sort edges
        int treeNum = 1;
        for(Edge e : edges) {
            if(mst.size() == nodes.size() - 1) { // stop when every node is in the mst
                break;
            }
            else if(nodes.get(e.nodeA) == 0 && nodes.get(e.nodeB) == 0) { // if both not in a tree add to same treeNum
                mst.add(e);
                nodes.replace(e.nodeA, treeNum);
                nodes.replace(e.nodeB, treeNum);
                treeNum++;
            }
            else if(nodes.get(e.nodeA) == 0 && nodes.get(e.nodeB) != 0) { // if A not in tree add to Bs tree
                mst.add(e);
                nodes.replace(e.nodeA, nodes.get(e.nodeB));
            }
            else if(nodes.get(e.nodeA) != 0 && nodes.get(e.nodeB) == 0) {// if B not in tree add to As tree
                mst.add(e);
                nodes.replace(e.nodeB, nodes.get(e.nodeA));
            }
            else if(nodes.get(e.nodeA) != nodes.get(e.nodeB)) { // if both in trees convert one of them to the other tree?
                mst.add(e);
                for(String node : nodes.keySet()){
                    if(nodes.get(node) == nodes.get(e.nodeA)) {
                        nodes.replace(node, nodes.get(e.nodeB));
                    }
                }
            }
        }
    }

    public void printMST() { // need to change to BFS format later
        for(Edge e : mst) {
            System.out.println(e);
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