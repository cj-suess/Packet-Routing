package csx55.overlay.node;

import csx55.overlay.transport.TCPReceiverThread;
import csx55.overlay.transport.TCPSender;
import csx55.overlay.util.Tuple;
import csx55.overlay.transport.TCPConnection;
import csx55.overlay.wireformats.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessagingNode implements Node {

    private volatile boolean registered;

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
    Map<String, TCPConnection> openConnections;
    Map<Socket, TCPConnection> socketToConn;


    public MessagingNode(String registryIP, int registryPort) {
        this.registryIP = registryIP;
        this.registryPort = registryPort;
        this.registered = false;
        connectionList = new ArrayList<>();
        openConnections = new ConcurrentHashMap<>();
        socketToConn = new ConcurrentHashMap<>();
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
            connect(conn.numConnections);
            System.out.printf("setup completed with %d connections\n", conn.numConnections);
        }
        else if(event.getType() == Protocol.NODE_ID){
            Message message = (Message) event;
            String remoteNodeID = message.info;
            if(socket != null) {
                TCPConnection conn = socketToConn.get(socket);
                openConnections.put(remoteNodeID, conn);
            }
        }
    }

    public void connect(int numConnections) {
        System.out.printf("Received %d connections from Registry...\n", numConnections);
        for(Tuple t : connectionList) {
            String remoteNodeID = t.getEndpoint();
            try {
                Socket socket = new Socket(t.getIp(), Integer.parseInt(t.getPort()));
                TCPConnection conn = new TCPConnection(socket, this);
                socketToConn.put(socket, conn);
                openConnections.put(remoteNodeID, conn);
                new Thread(conn).start();
                // send initial message with ip/server port so the receiving node can map the TCPConnection to it
                String localNodeID = InetAddress.getLocalHost().getHostAddress() + ":" + serverPort;
                Message idMessage = new Message(Protocol.NODE_ID, (byte)0, localNodeID);
                conn.sender.sendData(idMessage.getBytes());
            } catch(IOException e) {
                System.err.println("Failed to connect to " + t.getEndpoint() + ": " + e.getLocalizedMessage());
            }
        }
        System.out.println("All connections are established. Number of connections: " + numConnections);
    }

    public void printConnectionList() {
        System.out.println("Printing Connections: ");
        for(Map.Entry<String, TCPConnection> entry : openConnections.entrySet()) {
            System.out.println("Local: " + entry.getKey() + "  ->  Remote: " + entry.getValue().socket.getInetAddress().getHostAddress() + ":" + entry.getValue().socket.getPort());
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
                    if(registered == true) { deregister(); }
                    serverSocket.close();
                } catch(IOException e) {
                    System.err.println("Exception while trying to clean up after sudden termination...");
                }
            }));

            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("[MessagingNode] New connection on messaging node from: " + socket.getInetAddress());
                TCPConnection conn = new TCPConnection(socket, this); 
                socketToConn.put(socket, conn);
                new Thread(conn).start();
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
