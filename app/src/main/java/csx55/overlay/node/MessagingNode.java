package csx55.overlay.node;

import csx55.overlay.transport.TCPReceiverThread;
import csx55.overlay.transport.TCPSender;
import csx55.overlay.util.LogConfig;
import csx55.overlay.util.OverlayCreator;
import csx55.overlay.util.Tuple;
import csx55.overlay.spanning.MinimumSpanningTree;
import csx55.overlay.transport.TCPConnection;
import csx55.overlay.wireformats.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MessagingNode implements Node {

    // Node info
    private volatile boolean registered;
    ServerSocket serverSocket;
    int serverPort;

    // Registry info
    String registryIP;
    int registryPort;
    Socket registrySocket;
    TCPSender registrySender;
    TCPReceiverThread registryReceiver;

    // Connected messaging nodes
    List<Tuple> connectionList;
    Map<String, TCPConnection> openConnections;
    Map<Socket, TCPConnection> socketToConn;

    // Overlay
    Map<String, List<Tuple>> overlay;

    // MST
    MinimumSpanningTree mst;

    //Logging
    private volatile String nodeID = "NO ID";
    private Logger LOG = Logger.getLogger(MessagingNode.class.getName());


    public MessagingNode(String registryIP, int registryPort) {
        this.registryIP = registryIP;
        this.registryPort = registryPort;
        this.registered = false;
        connectionList = new ArrayList<>();
        openConnections = new ConcurrentHashMap<>();
        socketToConn = new ConcurrentHashMap<>();
        overlay = new HashMap<>();
    }

    public void onEvent(Event event, Socket socket) {
        if(event.getType() == Protocol.REGISTER_RESPONSE) {
            Message message = (Message) event; // downcast back to Message
            System.out.println(message.info);
            if(message.statusCode == (byte)0) { registered = true; }
        }
        else if(event.getType() == Protocol.DEREGISTER_RESPONSE) {
            Message message = (Message) event; // downcast back to Message
            System.out.println(message.info);
            if(message.statusCode == (byte)0) { registered = false; }
        }
        else if(event.getType() == Protocol.MESSAGING_NODES_LIST) {
            MessagingNodesList conn = (MessagingNodesList) event;
            connectionList = conn.getPeers();
            connect(conn.numConnections);
        }
        else if(event.getType() == Protocol.NODE_ID){
            Message message = (Message) event;
            String remoteNodeID = message.info;
            TCPConnection conn = socketToConn.get(socket);
            openConnections.put(remoteNodeID, conn);
        }
        else if(event.getType() == Protocol.OVERLAY) {
            Overlay o = (Overlay) event;
            overlay = o.overlay;
        }
        else if(event.getType() == Protocol.LINK_WEIGHTS) {
            System.out.println("Link weights received and processed. Ready to send messages.");
        }
    }

    public void connect(int numConnections) {
        LOG.info(() -> "Received " + numConnections + " conenctions from registry.");
        for(Tuple t : connectionList) {
            try {
                String remoteNodeID = t.getEndpoint();
                Socket socket = new Socket(t.getIp(), Integer.parseInt(t.getPort()));
                TCPConnection conn = new TCPConnection(socket, this);
                socketToConn.put(socket, conn);
                openConnections.put(remoteNodeID, conn);
                new Thread(conn, "Node-" + nodeID + " Conn-" + remoteNodeID).start();
                // send initial message with ip/server port so the receiving node can map the TCPConnection to it
                String localNodeID = InetAddress.getLocalHost().getHostAddress() + ":" + serverPort;
                Message idMessage = new Message(Protocol.NODE_ID, (byte)0, localNodeID);
                conn.sender.sendData(idMessage.getBytes());
            } catch(IOException e) {
                LOG.warning("Failed to connect to " + t.getEndpoint() + ": " + e.getLocalizedMessage());
            }
        }
        System.out.println("All connections are established. Number of connections: " + numConnections);
    }

    public void printConnectionList() {
        LOG.info("Printing My Connections: ");
        for(Map.Entry<String, TCPConnection> entry : openConnections.entrySet()) {
            LOG.info("Connected NodeID: " + entry.getKey());
        }
    }

    public void printOverlay() {
        for(Map.Entry<String, List<Tuple>> entry : overlay.entrySet()) {
            LOG.info(entry.toString());
        }
    }

    public void startNode() {
        try {
            serverSocket = new ServerSocket(0);
            serverPort = serverSocket.getLocalPort();
            serverSocket.getInetAddress();
            nodeID = InetAddress.getLocalHost().getHostAddress() + ":" + serverPort;
            LOG = Logger.getLogger(MessagingNode.class.getName() + "[" + nodeID + "]");
            LOG.info("Messaging node is up and running.\n \tListening on port: " + serverPort + "\n" + "\tIP Address: " + InetAddress.getLocalHost().getHostAddress());
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
                LOG.info("New connection on messaging node from: " + socket.getInetAddress());
                TCPConnection conn = new TCPConnection(socket, this); 
                socketToConn.put(socket, conn);
                new Thread(conn, nodeID).start();
            }

        } catch(IOException e) {
            LOG.warning("Exception while starting messaging node..." + e.getMessage());
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
                    case "print-overlay":
                        printOverlay();
                        break;
                    case "print-mst":
                        OverlayCreator oc = new OverlayCreator();
                        mst = new MinimumSpanningTree(overlay, oc);
                        mst.printMST();
                    default:
                        break;
                }
            }
        } catch(Exception e) {
            LOG.warning("Exception in terminal reader..." + e.getMessage());
        }
    }

    public void register() {
        try {
            if(registrySender == null) {
                registrySocket = new Socket(registryIP, registryPort);
                LOG.info("Connecting to registry...");
                Register registerRequest = new Register(Protocol.REGISTER_REQUEST, registrySocket.getLocalAddress().getHostAddress(), serverPort);
                registrySender = new TCPSender(registrySocket);
                registryReceiver = new TCPReceiverThread(registrySocket, this);
                new Thread(registryReceiver, "Node-" + nodeID + "-RegistryConn").start();
                registrySender.sendData(registerRequest.getBytes());
            } else {
                Register registerRequest = new Register(Protocol.REGISTER_REQUEST, registrySocket.getLocalAddress().getHostAddress(), serverPort);
                registrySender.sendData(registerRequest.getBytes());
            }
            
        } catch (Exception e) {
            LOG.warning("Exception while registering node with registry...");
        }
    }

    public void deregister() {
        LOG.info("Deregistering node...");
            Deregister deregisterRequest = new Deregister(Protocol.DEREGISTER_REQUEST, registrySocket.getLocalAddress().getHostAddress(), serverPort);
            try {
                registrySender.sendData(deregisterRequest.getBytes());
            } catch (IOException e) {
                LOG.warning("Exception while deregitering node..." + e.getMessage());
            }
    }

    public void nodeStatus() {
        LOG.info("Current node status: " + this.registered);
    }

    public static void main(String[] args) {

        LogConfig.init(Level.WARNING);

        MessagingNode node = new MessagingNode(args[0], Integer.parseInt(args[1]));
        new Thread(node::startNode, "Node-" + node.nodeID + "-Server").start();
        new Thread(node::readTerminal, "Node-" + node.nodeID + "-Terminal").start();
    }
    
}
