package csx55.overlay.wireformats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Queue;

public class Payload implements Event {

    public int messageType;
    public int payload;
    public Queue<String> path;

    public Payload(int messageType, int payload, Queue<String> path) {
        this.messageType = messageType;
        this.payload = payload;
        this.path = path;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] encodedData = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baos);
        
        dout.writeInt(messageType);
        dout.writeInt(payload);
        dout.writeInt(path.size());
        writeStrings(dout, path);

        dout.flush();
        encodedData = baos.toByteArray();
        baos.close();
        dout.close();
        return encodedData;
    }

    @Override
    public int getType() {
        return Protocol.PAYLOAD;
    }

    private static void writeStrings(DataOutputStream dout, Queue<String> path) throws IOException {
        for(String node : path){
            byte[] nodeBytes = node.getBytes();
            int nodeLength = nodeBytes.length;
            dout.writeInt(nodeLength);
            dout.write(nodeBytes);
        }
    }

}
