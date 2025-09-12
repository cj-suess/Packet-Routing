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

        // assign treeNum variable to each node with 0
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
            LOG.info(e.toString());
        }
    }
    
    // rough algo
        // treeNum = 1
        // lowestTreeNum = treeNum
        // while(set.size() != n - 1)
            //if(set empty and (nodeOne is null and nodeTwo is null))
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
        int lowest = treeNum;
        for(Edge e : edges) {
            if(mst.size() == nodes.size() - 1) { // stop when every node is in the mst
                break;
            }
            else if(mst.isEmpty() && (nodes.get(e.nodeA) == 0 && nodes.get(e.nodeB) == 0)) {
                mst.add(e);
                nodes.replace(e.nodeA, treeNum);
                nodes.replace(e.nodeB, treeNum);
            }
            else if(nodes.get(e.nodeA) == 0 || nodes.get(e.nodeB) == 0) {
                mst.add(e);
                for(Map.Entry<String, Integer> entry : nodes.entrySet()){
                    if(entry.getValue() > lowest) {
                        entry.setValue(lowest);
                    }
                }
                treeNum -= 1;
            }
            else if(!mst.isEmpty() && (nodes.get(e.nodeA) == 0 && nodes.get(e.nodeB) == 0)) {
                mst.add(e);
                treeNum += 1;
                nodes.replace(e.nodeA, treeNum);
                nodes.replace(e.nodeA, treeNum);
            }
            else {
                LOG.warning("No conditions met for building MST...");
                break;
            }
        }
    }

    public void printMST() { // need to change to BFS format later
        for(Edge e : mst) {
            System.out.println(e);
        }
    }


    // private class Node {
    //     String id;
    //     int treeNum;

    //     private Node(String id) {
    //         this.id = id;
    //         this.treeNum = 0;
    //     }

    //     public String getId() {
    //         return id;
    //     }

    //     public int getTreeNum() {
    //         return treeNum;
    //     }

    //     @Override
    //     public String toString() {
    //         return getId() + ", " + getTreeNum();
    //     }

    // }

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