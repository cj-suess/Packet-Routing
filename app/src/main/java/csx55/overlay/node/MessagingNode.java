package csx55.overlay.node;

import csx55.overlay.transport.TCPReceiverThread;
import csx55.overlay.transport.TCPSender;
import csx55.overlay.transport.TCPServerThread;
import csx55.overlay.wireformats.*;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

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
        if(event.getType() == Protocol.REGISTER_RESPONSE) {
            Message message = (Message) event; // downcast back to Message
            System.out.println(message.info);
        }
    }

    public void startNode() {
        try {
            serverSocket = new ServerSocket(0);
            serverPort = serverSocket.getLocalPort();
            System.out.println("[MessagingNode] Messaging node is up and running.\n \t[MessagingNode] Listening on port: " + serverPort + "\n" + "\t[MessagingNode] IP Address: " + serverSocket.getInetAddress().getHostAddress());

            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("[MessagingNode] New connection on messaging node from: " + socket.getInetAddress());
                TCPServerThread st = new TCPServerThread(socket, this);
                new Thread(st).start();
            }
        } catch(IOException e) {
            System.out.println("[MessagingNode] Exception while starting messaging node..." + e.getMessage());
        }
    }

    public void readTerminal() {
        try {
            Scanner scanner = new Scanner(System.in);
            while(true) {
                String command = scanner.nextLine();
                switch (command) {
                    case "exit":
                        System.out.println("[MessagingNode] Closing messaging node...");
                        System.exit(0);
                        scanner.close();
                        break;
                    default:
                        break;
                }
            }
        } catch(Exception e) {
            System.err.println("[MessagingNode] Exception in terminal reader..." + e.getMessage());
        }
    }

    public void register() {
        try {
            socket = new Socket(hostname, port);
            System.out.println("[MessagingNode] Connecting to registry...\n" + "[MessagingNode] Local Port: " + socket.getLocalPort() +"\n" + "[MessagingNode] Remote Port: " + port);
            // create register request instance
            Register registerRequest = new Register(Protocol.REGISTER_REQUEST, socket.getLocalAddress().getHostAddress(), serverPort);
            // create sender instance to send register request
            TCPSender sender = new TCPSender(socket);
            // create receiving thread for response?
            TCPReceiverThread receiver = new TCPReceiverThread(socket, this, sender);
            new Thread(receiver).start();
            sender.sendData(registerRequest.getBytes());
        } catch (IOException e) {
            System.out.println("[MessagingNode] Exception while registering node with registry...");
        }
    }

    public static void main(String[] args) {
        MessagingNode node = new MessagingNode(args[0], Integer.parseInt(args[1]));
        new Thread(node::startNode).start();
        new Thread(node::readTerminal).start();
        node.register();
    }
    
}
