package csx55.overlay.util;

import java.util.*;

public class OverlayCreator {

    Set<String> nodes;
    int n,k;
    Map<String, List<Tuple>> overlay;
    List<String> nodeList;

    public OverlayCreator(Set<String> nodes, int connections) {
        this.nodes = nodes;
        this.k = connections;
        this.n = nodes.size();
        overlay = new HashMap<>();
        nodeList = new ArrayList<>(nodes);
    }

    public Map<String, List<Tuple>> build() {
        if(n >= k+1 && ((n*k) % 2 == 0)) {
            for(int i=0; i < n; i++) {
                overlay.put(nodeList.get(i), new ArrayList<>());
                for(int j=1; j <= k/2; j++) {
                    overlay.get(nodeList.get(i)).add(new Tuple(nodeList.get((i+j) % n), null));
                    overlay.get(nodeList.get(i)).add(new Tuple(nodeList.get((i-j+n) % n), null));
                }
            }
            if(k % 2 == 1) {
                 for(int i = 0; i < nodeList.size(); i++) {
                    int j = (i + n/2) % n;
                    overlay.get(nodeList.get(i)).add(new Tuple(nodeList.get(j), null));
                }
            }
        } else {
            System.err.println("[Registry] Error. Cannot create overlay with current state.");
        }
        return overlay;
    }
    
}
