package csx55.overlay.util;

import java.util.*;

public class OverlayCreator {

    Set<String> nodes;
    int connections;

    public OverlayCreator(Set<String> nodes, int connections) {
        this.nodes = nodes;
        this.connections = connections;
    }

    public Map<String, Set<String>> build() {
        int n = nodes.size();
        Map<String, Set<String>> overlay = new HashMap<>();
        List<String> nodeList = new ArrayList<>(nodes);
        // if we have at least 10 messaging nodes registered and n >= k + 1 and nk is even
        if(n >= 10 && n >= connections+1 && ((n*connections) % 2 == 0)) {
            for(int i=0; i < nodeList.size(); i++) {
                String currentNode = nodeList.get(i);
                System.out.println(currentNode);
                overlay.put(currentNode, new HashSet<>());
            }
        } else {
            System.err.println("[Registry] Error. Cannot create overlay with current state.");
        }
        return overlay;
    }
    
}
