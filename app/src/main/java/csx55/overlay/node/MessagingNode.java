package csx55.overlay.node;

import csx55.overlay.transport.*;
import csx55.overlay.util.*;
import csx55.overlay.wireformats.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentHashMap;
import csx55.overlay.spanning.MinimumSpanningTree;
import java.util.concurrent.atomic.*;

public class MessagingNode implements Node {

    // Node info
    private volatile boolean registered;
    private volatile ServerSocket serverSocket;
    private volatile int serverPort;

    // Registry info
    private final String registryIP;
    private final int registryPort;
    private volatile Socket registrySocket;
    private volatile TCPSender registrySender;
    private volatile TCPReceiverThread registryReceiver;

    // Connected messaging nodes
    private final Map<String, TCPConnection> openConnections = new ConcurrentHashMap<>();
    private final Map<Socket, TCPConnection> socketToConn = new ConcurrentHashMap<>();

    // Overlay
    private volatile Map<String, List<Tuple>> overlay = Map.of();
    private volatile List<Tuple> connectionList = List.of();

    // MST
    private volatile MinimumSpanningTree mst;

    // Message tracking
    private volatile AtomicInteger sendTracker = new AtomicInteger(0); 
    private volatile AtomicInteger receiveTracker = new AtomicInteger(0);
    private volatile AtomicInteger relayTracker = new AtomicInteger(0);
    private volatile AtomicLong sendSummation = new AtomicLong(0);
    private volatile AtomicLong receiveSummation = new AtomicLong(0);

    //Logging
    private volatile String nodeID = "NO ID";
    private Logger log = Logger.getLogger(MessagingNode.class.getName());


    public MessagingNode(String registryIP, int registryPort) {
        this.registryIP = registryIP;
        this.registryPort = registryPort;
        this.registered = false;
    }

    public void onEvent(Event event, Socket socket) {
        if(event == null) {
            log.warning("Null event received from event factory...");
        }
        else if(event.getType() == Protocol.REGISTER_RESPONSE) {
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
            connectionList = Collections.unmodifiableList(conn.getPeers());
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
            overlay = Collections.unmodifiableMap(o.overlay);
        }
        else if(event.getType() == Protocol.LINK_WEIGHTS) {
            System.out.println("Link weights received and processed. Ready to send messages.");
        }
        else if(event.getType() == Protocol.TASK_INITIATE){
            TaskInitiate ti = (TaskInitiate) event;
            log.info("Received task initiate from Registry with " + ti.numRounds + " rounds...");
            sendMessages(ti.numRounds);
            sendTaskComplete();
        }
        else if(event.getType() == Protocol.PULL_TRAFFIC_SUMMARY) {
            try {
                log.info("Received task summary request from Registry. Sending back requested information...");
                TaskSummaryResponse response = new TaskSummaryResponse(Protocol.TRAFFIC_SUMMARY, serverPort, sendTracker.get(), receiveTracker.get(), sendSummation.get(), receiveSummation.get(), relayTracker.get());
                registrySender.sendData(response.getBytes());
            } catch(IOException e) {
                log.warning("Exception while sending TaskSummaryReport to Registry...." + e.getMessage());
            }
        }
        else if(event.getType() == Protocol.PAYLOAD){
            Payload payload = (Payload) event;
            processPayload(payload);
        }
    }

    private void processPayload(Payload incoming) {
        try {
            incoming.path.poll(); // consume self first?
            if(incoming.path.isEmpty()) {
                receiveSummation.addAndGet(incoming.payload);
                receiveTracker.getAndIncrement();
            } else {
                Payload outgoing = new Payload(Protocol.PAYLOAD, incoming.payload, incoming.path);
                openConnections.get(incoming.path.peek()).sender.sendData(outgoing.getBytes());
                relayTracker.getAndIncrement();
            }
        } catch(IOException e) {
            log.warning("Exception while processing payload..." + e.getMessage());
        }
    }

    private void sendMessages(int numRounds) {
        try {
            // convert to list to get random node
            List<String> keys = new ArrayList<>(overlay.keySet());
            keys.remove(nodeID); // remove self
            Random rand = new Random();
            for(int i = 0; i < numRounds; i++) {
                for(int j = 0; j < 5; j++) {
                    int randNum = rand.nextInt();
                    String randNode = keys.get(rand.nextInt(keys.size()));
                    LinkedList<String> path = mst.findPath(nodeID, randNode); // might need to change to something else
                    Payload payload = new Payload(Protocol.PAYLOAD, randNum, path);
                    openConnections.get(path.peek()).sender.sendData(payload.getBytes());
                    sendSummation.addAndGet(randNum);
                    sendTracker.getAndIncrement();
                }
            }
        } catch(IOException e) {
            log.warning("Exception while sending a message ocurred..." + e.getMessage());
        }
    }

    private void connect(int numConnections) {
        log.info(() -> "Received " + numConnections + " connections from registry.");
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
                log.warning("Failed to connect to " + t.getEndpoint() + ": " + e.getLocalizedMessage());
            }
        }
        System.out.println("All connections are established. Number of connections: " + numConnections);
    }

    private void printConnectionList() {
        log.info("Printing My Connections: ");
        for(Map.Entry<String, TCPConnection> entry : openConnections.entrySet()) {
            log.info("Connected NodeID: " + entry.getKey());
        }
    }

    private void printOverlay() {
        for(Map.Entry<String, List<Tuple>> entry : overlay.entrySet()) {
            log.info(entry.toString());
        }
    }

    private void startNode() {
        try {
            serverSocket = new ServerSocket(0);
            serverPort = serverSocket.getLocalPort();
            serverSocket.getInetAddress();
            nodeID = InetAddress.getLocalHost().getHostAddress() + ":" + serverPort;
            log = Logger.getLogger(MessagingNode.class.getName() + "[" + nodeID + "]");
            log.info("Messaging node is up and running.\n \tListening on port: " + serverPort + "\n" + "\tIP Address: " + InetAddress.getLocalHost().getHostAddress());
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
                log.info("New connection on messaging node from: " + socket.getInetAddress());
                TCPConnection conn = new TCPConnection(socket, this); 
                socketToConn.put(socket, conn);
                new Thread(conn, nodeID).start();
            }

        } catch(IOException e) {
            log.warning("Exception while starting messaging node..." + e.getMessage());
        }
    }

    private void readTerminal() {
        try(Scanner scanner = new Scanner(System.in)) {
            while(true) {
                String command = scanner.nextLine();
                switch (command) {
                    case "exit-overlay":
                        if(registered) { deregister(); }
                        System.out.println("exited overlay");
                        break;
                    case "register":
                        register();
                        break;
                    case "deregister":
                        deregister();
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
                        break;
                    case "print-data":
                        printData();
                        break;
                    default:
                        break;
                }
            }
        } 
    }

    private void printData() {
        log.info("Sent Tracker: " + sendTracker + "\n\tReceived Tracker: " + receiveTracker + "\n\tSent Summation: " + sendSummation + "\n\tReceived Summation: " + receiveSummation + "\n\tRelayed: " + relayTracker);
    }

    private void register() {
        try {
            if(registrySender == null) {
                registrySocket = new Socket(registryIP, registryPort);
                log.info("Connecting to registry...");
                Register registerRequest = new Register(Protocol.REGISTER_REQUEST, registrySocket.getLocalAddress().getHostAddress(), serverPort);
                registrySender = new TCPSender(registrySocket);
                registryReceiver = new TCPReceiverThread(registrySocket, this);
                new Thread(registryReceiver, "Node-" + nodeID + "-RegistryConn").start();
                registrySender.sendData(registerRequest.getBytes());
            } else {
                Register registerRequest = new Register(Protocol.REGISTER_REQUEST, registrySocket.getLocalAddress().getHostAddress(), serverPort);
                registrySender.sendData(registerRequest.getBytes());
            }
            
        } catch (IOException e) {
            log.warning("Exception while registering node with registry..." + e.getMessage());
        }
    }

    private void deregister() {
        log.info("Deregistering node...");
            Deregister deregisterRequest = new Deregister(Protocol.DEREGISTER_REQUEST, registrySocket.getLocalAddress().getHostAddress(), serverPort);
            try {
                registrySender.sendData(deregisterRequest.getBytes());
            } catch (IOException e) {
                log.warning("Exception while deregitering node..." + e.getMessage());
            }
    }

   private void sendTaskComplete() {
        try {
            TaskComplete tc = new TaskComplete(Protocol.TASK_COMPLETE, InetAddress.getLocalHost().getHostAddress(), serverPort);
            registrySender.sendData(tc.getBytes());
        } catch(IOException e) {
            log.warning(e.getMessage());
        }
   }

    public static void main(String[] args) {

        LogConfig.init(Level.WARNING);

        MessagingNode node = new MessagingNode(args[0], Integer.parseInt(args[1]));
        new Thread(node::startNode, "Node-" + node.nodeID + "-Server").start();
        new Thread(node::readTerminal, "Node-" + node.nodeID + "-Terminal").start();
    }
    
}
