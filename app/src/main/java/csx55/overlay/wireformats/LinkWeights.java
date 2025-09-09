package csx55.overlay.wireformats;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LinkWeights implements Event {

    int messageType;
    int dummyData;

    public LinkWeights(int messsageType, int dummyData) {
        this.messageType = messsageType;
        this.dummyData = dummyData;
    }

    @Override
    public int getType() {
        return Protocol.LINK_WEIGHTS;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] encodedData = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baos);
        dout.writeInt(messageType);
        /* FILL IN REQURED MARSHALING */
        dout.writeInt(dummyData);
        /*                           */
        dout.flush();
        encodedData = baos.toByteArray();
        baos.close();
        dout.close();
        return encodedData;
    }
    
}
