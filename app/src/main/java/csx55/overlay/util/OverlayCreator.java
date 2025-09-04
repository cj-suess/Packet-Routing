package csx55.overlay.util;

import java.util.*;

public class OverlayCreator {

    Set<String> nodes;
    int n,k;
    Map<String, Set<String>> overlay;
    List<String> nodeList;

    public OverlayCreator(Set<String> nodes, int connections) {
        this.nodes = nodes;
        this.k = connections;
        this.n = nodes.size();
        overlay = new HashMap<>();
        nodeList = new ArrayList<>(nodes);
    }

    public Map<String, Set<String>> build() {
        if(n >= k+1 && ((n*k) % 2 == 0)) {
            initializeOverlay(overlay, nodeList);
            for(int i=0; i < n; i++) {
                for(int j=1; j <= k/2; j++) {
                    overlay.get(nodeList.get(i)).add(nodeList.get((i+j) % n));
                    overlay.get(nodeList.get(i)).add(nodeList.get((i-j+n) % n));
                }
            }
            if(k % 2 == 1) {
                 for(int i = 0; i < nodeList.size(); i++) {
                    int j = (i + n/2) % n;
                    overlay.get(nodeList.get(i)).add((nodeList.get(j)));
                }
            }
        } else {
            System.err.println("[Registry] Error. Cannot create overlay with current state.");
        }
        return overlay;
    }

    public void initializeOverlay(Map<String, Set<String>> overlay, List<String> nodeList) {
        System.out.println("Initializing overlay....");
        for(int i=0; i < nodeList.size(); i++) {
                overlay.put(nodeList.get(i), new HashSet<>());
                overlay.get(nodeList.get(i)).add(nodeList.get((i+1) % n));
        }
    }
    
}
