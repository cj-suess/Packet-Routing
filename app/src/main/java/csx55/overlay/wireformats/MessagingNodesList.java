package csx55.overlay.wireformats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import csx55.overlay.util.Tuple;

public class MessagingNodesList implements Event {

    public int messageType;
    public int numConnections;
    List<Tuple> peers;

    public MessagingNodesList(int messageType, int numConnections, List<Tuple> peers) {
        this.messageType = messageType;
        this.numConnections = numConnections;
        this.peers = peers;
    }

    @Override
    public int getType() {
        return Protocol.MESSAGING_NODES_LIST;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] encodedData = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baos);
        dout.writeInt(messageType);
        /* FILL IN REQURED MARSHALING */
        dout.writeInt(numConnections);
        writeTuples(dout, peers);
        /*                           */
        dout.flush();
        encodedData = baos.toByteArray();
        baos.close();
        dout.close();
        return encodedData;
    }

    private void writeTuples(DataOutputStream dout, List<Tuple> peers) throws IOException {
        for(Tuple t : peers) {
            byte[] ipBytes = t.getEndpoint().getBytes();
            int ipLength = ipBytes.length;
            dout.writeInt(ipLength);
            dout.write(ipBytes);
            dout.writeInt(t.getWeight());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Peers: ");
        for(Tuple t : peers){
            sb.append(t.getEndpoint()).append(" (weight=").append(t.getWeight()).append(") ");
        }
        return sb.toString();
    }

    public List<Tuple> getPeers() {
        return peers;
    }
    
}
