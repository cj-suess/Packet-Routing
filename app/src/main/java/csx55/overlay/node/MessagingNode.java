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
    boolean registered;

    ServerSocket serverSocket;
    static int serverPort;

    // Registry info
    static Socket registrySocket;
    TCPSender registrySender;
    TCPReceiverThread registryReceiver;


    public MessagingNode(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.registered = false;
    }

    public void onEvent(Event event, TCPSender sender, Socket socket) {
        if(event.getType() == Protocol.REGISTER_RESPONSE) {
            Message message = (Message) event; // downcast back to Message
            System.out.println("[MessagingNode] " + message.info);
            if(message.statusCode == (byte)0) { registered = true; }
        }
        if(event.getType() == Protocol.DEREGISTER_RESPONSE) {
            Message message = (Message) event; // downcast back to Message
            System.out.println("[MessagingNode] " + message.info);
            if(message.statusCode == (byte)0) { registered = false; }
        }
    }

    public void startNode() {
        try {
            serverSocket = new ServerSocket(0);
            serverPort = serverSocket.getLocalPort();
            serverSocket.getInetAddress();
            // messaging nodes IP address: InetAddress.getLocalHost().getHostAddress()
            System.out.println("[MessagingNode] Messaging node is up and running.\n \tListening on port: " + serverPort + "\n" + "\tIP Address: " + InetAddress.getLocalHost().getHostAddress());
            register();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> { // needed if the terminal crashes so the node deregisters. not sure if I can catch it elsewhere
                try {
                    if(registered) { deregister(); }
                    serverSocket.close();
                } catch(IOException e) {
                    System.err.println("Exception while trying to clean up after sudden termination...");
                }
            }));

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
                    case "exit-overlay":
                        System.out.println("[MessagingNode] Closing messaging node...");
                        if(registered == true) { deregister(); }
                        System.out.println("exited overlay");
                        System.exit(0);
                        scanner.close();
                        break;
                    case "register":
                        register();
                        break;
                    // case "deregister":
                    //     deregister();
                    //     break;
                    case "node-status":
                        nodeStatus();
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
            if(registrySender == null) {
                registrySocket = new Socket(hostname, port);
                System.out.println("[MessagingNode] Connecting to registry...\n" + "\tLocal Port: " + registrySocket.getLocalPort() +"\n" + "\tRemote Port: " + port);
                // create register request instance
                Register registerRequest = new Register(Protocol.REGISTER_REQUEST, registrySocket.getLocalAddress().getHostAddress(), serverPort);
                // create sender instance to send register request
                registrySender = new TCPSender(registrySocket);
                // create receiving thread for response?
                registryReceiver = new TCPReceiverThread(registrySocket, this, registrySender);
                new Thread(registryReceiver).start();
                registrySender.sendData(registerRequest.getBytes());
            } else {
                Register registerRequest = new Register(Protocol.REGISTER_REQUEST, registrySocket.getLocalAddress().getHostAddress(), serverPort);
                registrySender.sendData(registerRequest.getBytes());
            }
            
        } catch (Exception e) {
            System.out.println("[MessagingNode] Exception while registering node with registry...");
        }
    }

    public void deregister() {
        System.out.println("[MessagingNode] Deregistering node...");
            Deregister deregisterRequest = new Deregister(Protocol.DEREGISTER_REQUEST, registrySocket.getLocalAddress().getHostAddress(), serverPort);
            try {
                registrySender.sendData(deregisterRequest.getBytes());
            } catch (IOException e) {
                System.err.println("Exception while deregitering node..." + e.getMessage());
            }
    }

    public void nodeStatus() {
        System.out.println("Current node status: " + this.registered);
    }

    public static void main(String[] args) {
        MessagingNode node = new MessagingNode(args[0], Integer.parseInt(args[1]));
        new Thread(node::startNode).start();
        new Thread(node::readTerminal).start();
    }
    
}
