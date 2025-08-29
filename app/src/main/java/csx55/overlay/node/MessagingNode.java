package csx55.overlay.node;

import csx55.overlay.transport.TCPReceiverThread;
import csx55.overlay.transport.TCPSender;
import csx55.overlay.transport.TCPServerThread;
import csx55.overlay.wireformats.*;
import java.io.IOException;
import java.net.*;

public class MessagingNode implements Node {

    String hostname;
    int port;

    ServerSocket serverSocket;
    int serverPort;
    TCPServerThread st;
    Socket socket;


    public MessagingNode(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void onEvent(Event event) {
        // if event == Register
        if(event.getType() == 0) {
            // send register request to registry
        }
        // if event == Deregister
            // send deregister request to registry
    }

    public void startNode() {
        try {
            serverSocket = new ServerSocket(0);
            serverPort = serverSocket.getLocalPort();
            System.out.println("Messaging node is up and running. Listening on port: " + serverPort);

            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection on messaging node from: " + socket.getInetAddress());
                st = new TCPServerThread(socket);
                new Thread(st).start();
            }
        } catch(IOException e) {
            System.out.println("Exception while starting messaging node..." + e.getMessage());
        }
    }

    public void register() {
        try {
            socket = new Socket(hostname, port);
            System.out.println("Connected to registry...");
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public static void main(String[] args) {
        MessagingNode node = new MessagingNode(args[0], Integer.parseInt(args[1]));
        new Thread(node::startNode).start();
        node.register();
    }
    
}
