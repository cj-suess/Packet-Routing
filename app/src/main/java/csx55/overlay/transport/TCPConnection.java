package csx55.overlay.transport;

import java.io.IOException;
import java.net.Socket;
import csx55.overlay.node.Node;

public class TCPConnection implements Runnable {

    public Socket socket;
    public TCPReceiverThread receiver;
    public TCPSender sender;
    public Node node;

    public TCPConnection(Socket socket, Node node) throws IOException {
        this.socket = socket;
        this.node = node;
        this.sender = new TCPSender(socket);
        this.receiver = new TCPReceiverThread(socket, node);
    }

    @Override
    public void run() {
        try {
            new Thread(receiver).start();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        } 
    }

}
