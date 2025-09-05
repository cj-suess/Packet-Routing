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
                    overlay.get(nodeList.get(i)).add(new Tuple(nodeList.get((i+j) % n), 0));
                    overlay.get(nodeList.get(i)).add(new Tuple(nodeList.get((i-j+n) % n), 0));
                }
            }
            if(k % 2 == 1) {
                 for(int i = 0; i < nodeList.size(); i++) {
                    int j = (i + n/2) % n;
                    overlay.get(nodeList.get(i)).add(new Tuple(nodeList.get(j), 0));
                }
            }
        } else {
            System.err.println("[Registry] Error. Cannot create overlay with current state.");
        }
        System.out.printf("setup completed with %d connections\n", k);
        return overlay;
    }

    // filter method before sending connection requests
    public Map<String, List<Tuple>> filter() {
        Map<String, List<Tuple>> connectionMap = new HashMap<>();
        for(Map.Entry<String, List<Tuple>> entry : overlay.entrySet()){
            List<Tuple> filtered = new ArrayList<>();
            for(Tuple t : entry.getValue()) {
                if(entry.getKey().compareTo(t.endpoint) < 0) {
                    filtered.add(t);
                }
            }
            connectionMap.put(entry.getKey(), filtered);
        }
        System.out.println("Overlay has been filtered into connectionMap for MessagingNodesList message...");
        return connectionMap;
    }
    
}
