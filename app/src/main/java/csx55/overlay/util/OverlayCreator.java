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
            System.out.println("Initializing overlay....");
            for(int i=0; i < nodeList.size(); i++) {
                String currentNode = nodeList.get(i);
                overlay.put(currentNode, new HashSet<>());
            }
            // for loop to create circle
            System.out.println("Creating circular graph...");
            for(int i = 0; i < nodeList.size(); i++) {
                // mirror the connection in both lists
                overlay.get(nodeList.get(i)).add(nodeList.get((i+1) % n));
                overlay.get(nodeList.get((i+1) % n)).add(nodeList.get(i));
            }
            // if n is even
            if(n % 2 == 0) {
                // if k is even
                System.out.println("N and K are even...");
                if(k % 2 == 0) {
                    // i -> (i+3) % n
                    int i = 0;
                    while(overlay.get(nodeList.get(i)).size() < k) {
                        overlay.get(nodeList.get(i)).add(nodeList.get((i+3) % n));
                        overlay.get(nodeList.get((i+3) % n)).add(nodeList.get(i));
                        i = (i+1) % n;
                    }
                } 
                // if k is odd
                else if(k % 2 == 1) {
                    System.out.println("N is even and K is odd...");
                    // build even for k-1
                    int i = 0;
                    while(overlay.get(nodeList.get(i)).size() < k-1) {
                        overlay.get(nodeList.get(i)).add(nodeList.get((i+3) % n));
                        overlay.get(nodeList.get((i+3) % n)).add(nodeList.get(i));
                        i = (i+1) % n;
                    }
                    // one more loop adding cross connections for k
                    for(i = 0; i < nodeList.size(); i++) {
                        int j = (i + n/2) % n;
                        if(overlay.get(nodeList.get(i)).size() < k && overlay.get(nodeList.get(j)).size() < k) {
                            overlay.get(nodeList.get(i)).add((nodeList.get(j)));
                            overlay.get(nodeList.get(j)).add((nodeList.get(i)));
                        }
                    }
                }
            }
            // if n is odd
            else if(n % 2 == 1) {
                System.out.println("N is odd...");
                // k has to be even
                // i -> (i+3) % n
                int i = 0;
                while(overlay.get(nodeList.get(i)).size() < k) {
                    overlay.get(nodeList.get(i)).add(nodeList.get((i+3) % n));
                    overlay.get(nodeList.get((i+3) % n)).add(nodeList.get(i));
                    i = (i+1) % n;
                }
            }
        } else {
            System.err.println("[Registry] Error. Cannot create overlay with current state.");
        }
        return overlay;
    }
    
}
