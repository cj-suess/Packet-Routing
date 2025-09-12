package csx55.overlay.spanning;

import java.util.*;

import csx55.overlay.util.Tuple;

public class MinimumSpanningTree {

    Map<String, List<Tuple>> overlay;
    List<NodeID> nodes;

    public MinimumSpanningTree(Map<String, List<Tuple>> overlay) {
        this.overlay = overlay;
        this.nodes = new ArrayList<>();

        for(Map.Entry<String, List<Tuple>> entry : overlay.entrySet()){
            NodeID node = new NodeID(entry.getKey());
            nodes.add(node);
        }
    }

    public void printNodes() {
        for(NodeID node : nodes) {
            System.out.println(node.toString());
        }
    }
    
    // filter(overlay) -> filteredOverlay
        // sort(filtered) by weight

    // assign treeNum variable to each edge with null

    
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


    private class NodeID {
        String id;
        int treeNum;

        private NodeID(String id) {
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
}