package csx55.overlay.wireformats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskSummaryResponse implements Event {

    int messageType;
    public int serverPort;
    public int sendTracker; 
    public int receiveTracker;
    public long sendSummation;
    public long receiveSummation;
    public int relayTracker;

    public TaskSummaryResponse(int messageType, int serverPort, int sendTracker, int receiveTracker, long sendSummation, long receiveSummation, int relayTracker) {
        this.messageType = messageType;
        this.serverPort = serverPort;
        this.sendTracker = sendTracker;
        this.receiveTracker = receiveTracker;
        this.sendSummation = sendSummation;
        this.receiveSummation = receiveSummation;
        this.relayTracker = relayTracker;
    }

    @Override
    public int getType() {
        return Protocol.TRAFFIC_SUMMARY;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] encodedData = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baos);
        
        dout.writeInt(messageType);
        dout.writeInt(serverPort);
        dout.writeInt(sendTracker);
        dout.writeInt(receiveTracker);
        dout.writeLong(sendSummation);
        dout.writeLong(receiveSummation);
        dout.writeInt(relayTracker);
        
        dout.flush();
        encodedData = baos.toByteArray();
        baos.close();
        dout.close();
        return encodedData;
    }
    
}
