package csx55.overlay.wireformats;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EventFactory {

    byte[] data;
    int messageType;

    public EventFactory(byte[] data) {
        this.data = data;
    }

    public Event createEvent() {

        try {

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            messageType = dis.readInt();

            switch (messageType) {
                case Protocol.REGISTER_REQUEST:
                    // decode data into Register event
                    System.out.println("Decoding data into a Register object...");
                    String ip;
                    int port;
                    int ipLength = dis.readInt();
                    byte[] ipBytes = new byte[ipLength];
                    dis.readFully(ipBytes);
                    ip = new String(ipBytes);
                    port = dis.readInt();
                    bais.close();
                    dis.close();
                    Register register_request = new Register(messageType, ip, port);
                    return register_request;
                default:
                    throw new IllegalArgumentException("Unknown protocol passed to EventFactory...");
            }
        } catch(IOException e) {
            System.out.println("Exception while decoding data...");
        }
        return null;
    }

}
