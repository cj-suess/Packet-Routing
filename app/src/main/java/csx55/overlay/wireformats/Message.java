package csx55.overlay.wireformats;

public class Message {
    
    int messageType;
    String ip;
    int port;

    public Message(int messageType, String ip, int port) {
        this.messageType = messageType;
        this.ip = ip;
        this.port = port;
    }

}
