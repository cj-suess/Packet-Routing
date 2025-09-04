package csx55.overlay.transport;

import java.net.Socket;
import csx55.overlay.node.Node;

public class TCPServerThread implements Runnable {

    public Socket socket;
    public TCPReceiverThread receiver;
    public TCPSender sender;
    public Node node;

    public TCPServerThread(Socket socket, Node node) {
        this.socket = socket;
        this.node = node;
    }

    @Override
    public void run() {
        try {

            System.out.println("[TCPServerThread] New TCP Connection thread created...\n" + 
                                "\tLocal Port: " + socket.getLocalPort() + "\n" + 
                                "\tRemote Port: " + socket.getPort() + "\n");
            sender = new TCPSender(socket);
            receiver = new TCPReceiverThread(socket, node, sender);
            new Thread(receiver).start();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        } 
    }

}
