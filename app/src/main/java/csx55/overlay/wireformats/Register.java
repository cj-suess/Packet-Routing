package csx55.overlay.wireformats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Register implements Event, Protocol {

    public int messageType;
    public String ip;
    public int port;

    public Register(int messageType, String ip, int port) {
        this.messageType = messageType;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public byte[] getBytes() throws IOException{
        byte[] encodedData = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baos);
        dout.writeInt(messageType);

        byte[] ipBytes = ip.getBytes();
        int ipLength = ipBytes.length;
        dout.writeInt(ipLength);
        dout.write(ipBytes);

        dout.write(port);
        dout.flush();
        encodedData = baos.toByteArray();
        
        baos.close();
        dout.close();
        return encodedData;
    }

    @Override
    public int getType() {
        return 0;
    }

    public static void main(String[] args) {
        Register register = new Register(REGISTER_REQUEST, "grouper", 5000);
        try {
            byte[] packedData = register.getBytes();
            System.out.println(packedData);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
}
