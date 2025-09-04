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
        int k = connections;
        Map<String, Set<String>> overlay = new HashMap<>();
        List<String> nodeList = new ArrayList<>(nodes);
        // if we have at least 10 messaging nodes registered and n >= k + 1 and nk is even
        if(n >= 10 && n >= k+1 && ((n*k) % 2 == 0)) {
            for(int i=0; i < nodeList.size(); i++) {
                String currentNode = nodeList.get(i);
                overlay.put(currentNode, new HashSet<>());
            }
            // if n is even
            if(n % 2 == 0) {
                // for loop to create circle
                for(int i = 0; i < nodeList.size(); i++) {
                    // mirror the connection in both lists
                    overlay.get(nodeList.get(i)).add(nodeList.get((i+1) % n));
                    overlay.get(nodeList.get((i+1) % n)).add(nodeList.get(i));
                }
                // if k is even
                if(k % 2 == 0) {
                    // i -> (i+3) % n
                    int i = 0;
                    while(overlay.get(nodeList.get(i)).size() < k) {
                        overlay.get(nodeList.get(i)).add(nodeList.get((i+3) % n));
                        overlay.get(nodeList.get((i+3) % n)).add(nodeList.get(i));
                        i++;
                    }
                } 
                // if k is odd
                else if(k % 2 == 1) {
                    // i -> (i+2) % n
                }
            }
            // if n is odd
                // for loop to create circle
                    // k has to be even
                        // i -> (i+3) % n
        } else {
            System.err.println("[Registry] Error. Cannot create overlay with current state.");
        }
        return overlay;
    }
    
}
