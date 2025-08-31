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
    static int serverPort;
    static Socket socket;


    public MessagingNode(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void onEvent(Event event, TCPSender sender) {
        System.out.println("Event received: " + event.getType());
        // if event == Register
        if(event.getType() == Protocol.REGISTER_RESPONSE) {
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
                TCPServerThread st = new TCPServerThread(socket, this);
                new Thread(st).start();
            }
        } catch(IOException e) {
            System.out.println("Exception while starting messaging node..." + e.getMessage());
        }
    }

    public void register() {
        try {
            socket = new Socket(hostname, port);
            System.out.println("Connecting to registry...\n" + "Local Port: " + socket.getLocalPort() +"\n" + "Remote Port: " + port);
            // create register request instance
            Register registerRequest = new Register(Protocol.REGISTER_REQUEST, socket.getLocalAddress().getHostAddress(), serverPort);
            // create sender instance to send register request
            TCPSender sender = new TCPSender(socket);
            // create receiving thread for response?
            TCPReceiverThread receiver = new TCPReceiverThread(socket, this, sender);
            new Thread(receiver).start();
            sender.sendData(registerRequest.getBytes());
        } catch (IOException e) {
            System.out.println("Exception while registering node with registry...");
        }
    }

    public static void main(String[] args) {
        MessagingNode node = new MessagingNode(args[0], Integer.parseInt(args[1]));
        new Thread(node::startNode).start();
        node.register();
    }
    
}
