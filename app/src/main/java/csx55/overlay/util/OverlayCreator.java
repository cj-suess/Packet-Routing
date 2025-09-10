package csx55.overlay.util;

import java.util.*;
import java.util.logging.*;

public class OverlayCreator {

    Set<String> nodes;
    int n,k;
    Map<String, List<Tuple>> overlay;
    List<String> nodeList;

    private static Logger LOG = Logger.getLogger(OverlayCreator.class.getName());

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
                    overlay.get(nodeList.get(i)).add(new Tuple(nodeList.get((i+j) % n), (new Random().nextInt(9)) + 1));
                    overlay.get(nodeList.get(i)).add(new Tuple(nodeList.get((i-j+n) % n), (new Random().nextInt(9)) + 1));
                }
            }
            if(k % 2 == 1) {
                 for(int i = 0; i < nodeList.size(); i++) {
                    int j = (i + n/2) % n;
                    overlay.get(nodeList.get(i)).add(new Tuple(nodeList.get(j), (new Random().nextInt(9)) + 1));
                }
            }
            LOG.info("Overlay build complete...");
            return overlay;
        } else {
            LOG.warning("Error. Cannot create overlay with current state.");
        }
        return null;
    }

    // filter method before sending connection requests
    public static Map<String, List<Tuple>> filter(Map<String, List<Tuple>> overlay) {
        LOG.info("Beginning overlay filtering...");
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
        LOG.info("Overlay has been filtered into connectionMap for MessagingNodesList message...");
        return connectionMap;
    }
    
}
