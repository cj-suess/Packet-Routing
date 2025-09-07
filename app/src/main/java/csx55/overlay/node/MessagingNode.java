package csx55.overlay.node;

import csx55.overlay.transport.TCPReceiverThread;
import csx55.overlay.transport.TCPSender;
import csx55.overlay.util.Tuple;
import csx55.overlay.transport.TCPConnection;
import csx55.overlay.wireformats.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MessagingNode implements Node {

    boolean registered; // make atomic?

    ServerSocket serverSocket;
    int serverPort;

    // Registry info
    String registryIP;
    int registryPort;
    Socket registrySocket;
    TCPSender registrySender;
    TCPReceiverThread registryReceiver;

    // Messaging nodes
    List<Tuple> connectionList;
    List<Socket> openConnections;


    public MessagingNode(String registryIP, int registryPort) {
        this.registryIP = registryIP;
        this.registryPort = registryPort;
        this.registered = false;
        connectionList = new ArrayList<>();
        openConnections = new ArrayList<>();
    }

    public void onEvent(Event event, Socket socket) {
        if(event.getType() == Protocol.REGISTER_RESPONSE) {
            Message message = (Message) event; // downcast back to Message
            System.out.println("[MessagingNode] " + message.info);
            if(message.statusCode == (byte)0) { registered = true; }
        }
        else if(event.getType() == Protocol.DEREGISTER_RESPONSE) {
            Message message = (Message) event; // downcast back to Message
            System.out.println("[MessagingNode] " + message.info);
            if(message.statusCode == (byte)0) { registered = false; }
        }
        else if(event.getType() == Protocol.MESSAGING_NODES_LIST) {
            MessagingNodesList conn = (MessagingNodesList) event;
            connectionList = conn.getPeers();
        }
    }

    public synchronized void connect(){
        for(Tuple t : connectionList) {
            try {
                Socket socket = new Socket(t.getIp(), Integer.parseInt(t.getPort()));
                TCPSender sender = new TCPSender(socket);
                TCPReceiverThread receiver = new TCPReceiverThread(socket, this);
                new Thread(receiver).start();
                openConnections.add(socket);
            } catch(IOException e) {
                System.err.println("Exception while creating new socket for node to node conneciton..." + e.getLocalizedMessage());
            }
        }
    }

    public void printConnectionList() {
        System.out.println("Printing Connections...");
        for(Socket s : openConnections) {
            System.out.println("Local Address: " + s.getLocalAddress() + " Connection Address: " + s.getInetAddress().getHostAddress());
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
                TCPConnection st = new TCPConnection(socket, this); 
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
                        if(registered == true) { deregister(); }
                        System.out.println("exited overlay");
                        break;
                    case "register":
                        register();
                        break;
                    case "deregister":
                        deregister();
                        break;
                    case "node-status":
                        nodeStatus();
                        break;
                    case "connect":
                        connect();
                        break;
                    case "print-connections":
                        printConnectionList();
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
                registrySocket = new Socket(registryIP, registryPort);
                System.out.println("[MessagingNode] Connecting to registry...\n" + "\tLocal Port: " + registrySocket.getLocalPort() +"\n" + "\tRemote Port: " + registryPort);
                Register registerRequest = new Register(Protocol.REGISTER_REQUEST, registrySocket.getLocalAddress().getHostAddress(), serverPort);
                registrySender = new TCPSender(registrySocket);
                registryReceiver = new TCPReceiverThread(registrySocket, this);
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
