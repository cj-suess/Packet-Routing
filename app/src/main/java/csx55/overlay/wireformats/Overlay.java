package csx55.overlay.wireformats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import csx55.overlay.util.Tuple;

public class Overlay implements Event {
    
    public int messageType;
    public int numNodes;
    public int numConnections;
    public Map<String, List<Tuple>> overlay;

    public Overlay(int messageType, int numNodes, int numConnections, Map<String, List<Tuple>> overlay) {
        this.messageType = messageType;
        this.numNodes = numNodes;
        this.numConnections = numConnections;
        this.overlay = overlay;
    }

    @Override
    public int getType() {
        return Protocol.OVERLAY;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] encodedData = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baos);
        dout.writeInt(messageType);
        /* FILL IN REQURED MARSHALING */
        dout.writeInt(numNodes);
        dout.writeInt(numConnections);
        writeMappings(dout, overlay);
        /*                           */
        dout.flush();
        encodedData = baos.toByteArray();
        baos.close();
        dout.close();
        return encodedData;
    }

    // method to write each String -> List mapping
    private void writeMappings(DataOutputStream dout, Map<String, List<Tuple>> overlay) throws IOException {
        for(Map.Entry<String, List<Tuple>> entry : overlay.entrySet()) {
            byte[] ipBytes = entry.getKey().getBytes();
            int ipLength = ipBytes.length;
            dout.writeInt(ipLength);
            dout.write(ipBytes);
            for(Tuple t : entry.getValue()) {
                ipBytes = t.getEndpoint().getBytes();
                ipLength = ipBytes.length;
                dout.writeInt(ipLength);
                dout.write(ipBytes);
                dout.writeInt(t.getWeight());
            }
        }
    }
}
