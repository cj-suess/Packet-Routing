package csx55.overlay.node;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;

import csx55.overlay.transport.*;
import csx55.overlay.util.LogConfig;
import csx55.overlay.util.OverlayCreator;
import csx55.overlay.util.Tuple;
import csx55.overlay.wireformats.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Registry implements Node {

    int port;
    ServerSocket serverSocket;

    List<TCPConnection> openConnections;
    int connections = 0;

    Set<String> registeredNodes;
    Map<String, List<Tuple>> overlay; // grab messaging node ip and match with correct socket in openConnections
    Map<String, List<Tuple>> connectionMap; // use for relaying who connects to who to avoid duplicate connections

    //Logging
    private Logger log = Logger.getLogger(Registry.class.getName());

    public Registry(int port) {
        this.port = port;
        registeredNodes = ConcurrentHashMap.newKeySet(); // should work better using a single String?
        openConnections =  new ArrayList<>();
    }

    public void onEvent(Event event, Socket socket) {

        TCPSender sender = getSender(openConnections, socket);
        String socketAddress = socket.getInetAddress().getHostAddress(); // messaging nodes IP address

        try {
            if(event.getType() == Protocol.REGISTER_REQUEST) {
                log.info("Register request detected. Checking status...");
                Register node = (Register) event; // downcast back to Register
                String nodeEntry = node.ip + ":" + node.port;
                if(node.ip.equals(socketAddress) && registeredNodes.add(nodeEntry)) {
                    log.info(() -> "New node was added to the registry successfully!\n" + "\tCurrent number of nodes in registry: " + registeredNodes.size());
                    String info = "Registration request successful. The number of messaging nodes currently constituting the overlay is (" + registeredNodes.size() + ")";
                    Message successMessage = new Message(Protocol.REGISTER_RESPONSE, (byte)0, info);
                    log.info(() -> "Sending success response to messaging node at %s" + nodeEntry);
                    sender.sendData(successMessage.getBytes());
                } else {
                    // add failure cases
                    log.info(() -> "Failure ocurred while registering node...\n\tChecking for mismatching IPs...\n\tIP in message: " + node.ip + " Socket IP: " + socketAddress);
                    if(!node.ip.equals(socketAddress)) {
                        // send failure for mismatch
                        log.info("Sending failure message for mismatching IPs...");
                        String info = "Registration request failed. The IP address in the registration request did not match the IP address of the socket.";
                        Message failureMessage = new Message(Protocol.REGISTER_RESPONSE, (byte)1, info);
                        sender.sendData(failureMessage.getBytes());
                    } else {
                        // send failure for duplicate
                        log.info("Sending failure message for duplicate IPs...");
                        String info = "Registration request failed. The node entry already exists in the registry.";
                        Message failureMessage = new Message(Protocol.REGISTER_RESPONSE, (byte)1, info);
                        sender.sendData(failureMessage.getBytes());

                    }
                }
            }
            else if(event.getType() == Protocol.DEREGISTER_REQUEST) {
                log.info("Deregister request detected. Checking status...");
                Deregister node = (Deregister) event; // downcast back to Deregister
                String nodeEntry = node.ip + ":" + node.port;
                if(registeredNodes.remove(nodeEntry) && node.ip.equals(socketAddress)) {
                    log.info(() -> "The node at " + nodeEntry + " has been removed from the registry...");
                    String info = "The node has been successfully removed from the registry...";
                    Message successMessage = new Message(Protocol.DEREGISTER_RESPONSE, (byte)0, info);
                    log.info(() -> "Sending deregistration success response to messaging node at " + nodeEntry);
                    sender.sendData(successMessage.getBytes());
                } else {
                    if(!node.ip.equals(socketAddress)){
                        // mismatch
                        log.info("Sending failure message for mismatching IPs...");
                        String info = "Deregistration request failed. The IP address in the deregistration request did not match the IP address of the socket.";
                        Message failureMessage = new Message(Protocol.DEREGISTER_RESPONSE, (byte)1, info);
                        log.info(() -> "Sending deregistration failure response to messaging node at " + nodeEntry);
                        sender.sendData(failureMessage.getBytes());
                    } else {
                        // node entry does not exist
                        log.info("The node " + nodeEntry + " does not exist in the registry...");
                        String info = "The node could not be removed from the registry since it was not registered...";
                        Message failureMessage = new Message(Protocol.DEREGISTER_RESPONSE, (byte)1, info);
                        log.info(() -> "Sending deregistration failure response to messaging node at " + nodeEntry);
                        sender.sendData(failureMessage.getBytes());
                    }
                }
            }
            else if(event.getType() == Protocol.TASK_COMPLETE) {
                TaskComplete taskComplete = (TaskComplete) event;
                String nodeID = taskComplete.ip + ":" + taskComplete.port;
                // mark node as complete
                log.info("Received task complete message from " + nodeID);
                log.info("Need to implement marking node as complete still...");
            }
        } catch(IOException e) {
            log.warning("Exception in registery while handling an event...");
        }
    }

    private TCPSender getSender(List<TCPConnection> openConnections, Socket socket) {
        TCPSender sender = null;
        for(TCPConnection conn : openConnections) {
            if(socket == conn.socket) {
                sender = conn.sender;
            }
        }
        return sender;
    }

    public void startRegistry() {
        try {
            serverSocket = new ServerSocket(port);
            log.info("Registry is up and running. Listening on port: " + port);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> { // needed if the terminal crashes so the node deregisters. not sure if I can catch it elsewhere
                try {
                    serverSocket.close();
                } catch(IOException e) {
                    log.warning("Exception while trying to clean up after sudden termination...");
                }
            }));

            while(true) {
                Socket socket = serverSocket.accept();
                log.info(() -> "New connection from: " + socket.getInetAddress().getHostAddress());
                TCPConnection conn = new TCPConnection(socket, this);
                new Thread(conn).start();
                openConnections.add(conn);
            }

        } catch(IOException e) {
            log.warning(() -> "Exception in registry node loop..." + e.getMessage());
        }
    }

    public void readTerminal() {
        try {
            Scanner scanner = new Scanner(System.in);
            while(true) {
                String command = scanner.nextLine();
                String[] splitCommand = command.split("\\s+");
                switch (splitCommand[0]) {
                    case "exit":
                        log.info("[Registry] Closing registry node...");
                        scanner.close();
                        System.exit(0);
                        break;
                    case "list-messaging-nodes":
                        printRegistry();
                        break;
                    case "setup-overlay":
                        if(splitCommand.length > 1) {
                            connections = Integer.parseInt(splitCommand[1]);
                        }
                        OverlayCreator oc = new OverlayCreator(registeredNodes, connections);
                        overlay = oc.build();
                        if(overlay != null) {
                            connectionMap = oc.filter(overlay);
                            sendOverlay();
                            sendConnections();
                            System.out.println("setup completed with " + oc.totalConnectionsMade + " connections");
                        }
                        break;
                    case "list-weights":
                        listWeights();
                        break;
                    case "send-overlay-link-weights":
                        sendLinkWeights();
                        System.out.println("link weights assigned");
                        break;
                    case "start":
                        int numRounds = 0;
                        if(splitCommand.length > 1) {
                            numRounds = Integer.parseInt(splitCommand[1]);
                        }
                        sendTaskInitiate(numRounds);
                        break;
                    case "print-connections":
                        printConnections();
                        break;
                    case "print-overlay":
                        printOverlay();
                        break;
                    case "print-connection-map":
                        printConnectionMap();
                        break;
                    case "test-tsr":
                        sendTrafficSummaryRequest();
                        break;
                    default:
                        break;
                }
            }
        } catch(Exception e) {
            System.err.println("[Registry] Exception in terminal reader...");
        }
    }

    private void sendTrafficSummaryRequest() throws IOException {
        log.info("Sending request for traffic summary to messaginge nodes...");
        TaskSummaryRequest tsr = new TaskSummaryRequest(Protocol.PULL_TRAFFIC_SUMMARY);
        for(TCPConnection conn : openConnections) {
            conn.sender.sendData(tsr.getBytes());
        }
    }

    private void sendTaskInitiate(int numRounds) throws IOException {
        log.info("Sending task initate command to messaging nodes...");
        TaskInitiate ti = new TaskInitiate(Protocol.TASK_INITIATE, numRounds);
        for(TCPConnection conn : openConnections) {
            conn.sender.sendData(ti.getBytes());
        }
    }
    
    public void sendLinkWeights() throws IOException {
        LinkWeights lw = new LinkWeights(Protocol.LINK_WEIGHTS, 0);
        for(TCPConnection conn : openConnections) {
            conn.sender.sendData(lw.getBytes());
        }
    }

    public void sendOverlay() throws IOException {
        log.info("Sending overlay to messaging nodes...");
        log.info("\tNum connections: " + overlay.values().size());
        Overlay overlayMessage = new Overlay(Protocol.OVERLAY, registeredNodes.size(), connections, overlay);
        for(TCPConnection conn : openConnections) {
            conn.sender.sendData(overlayMessage.getBytes());
        }

    }

    public void sendConnections() throws IOException {
        log.info("Sending connections to the messaging nodes...");
        for(Map.Entry<String, List<Tuple>> entry : connectionMap.entrySet()) {
            String nodeIP = entry.getKey().substring(0, entry.getKey().indexOf(":"));
            int numConnections = entry.getValue().size();
            List<Tuple> peers = entry.getValue();
            MessagingNodesList instructions = new MessagingNodesList(Protocol.MESSAGING_NODES_LIST, numConnections, peers);
            for(TCPConnection conn : openConnections){
                if(nodeIP.equals(conn.socket.getInetAddress().getHostAddress())) {
                    conn.sender.sendData(instructions.getBytes());
                }
            }
        }

    }

    public void printRegistry() {
        registeredNodes.forEach(key -> System.out.println(key));
    }

    public void printConnections() {
        for(TCPConnection conn : openConnections) {
            log.warning(conn.socket.getInetAddress().getHostAddress());
        }
    }

    public void printOverlay() {
        for(Map.Entry<String, List<Tuple>> entry : overlay.entrySet()) {
            log.warning(entry.toString());
        }
    }

    public void printConnectionMap() {
        for(Map.Entry<String, List<Tuple>> entry : connectionMap.entrySet()){
            log.warning(entry.toString());
        }
    }

    public void listWeights() {
        for(Map.Entry<String, List<Tuple>> entry : connectionMap.entrySet()){
            for(Tuple t : entry.getValue()){
                System.out.println(entry.getKey() + ", " + t);
            }
        }
    }

    public static void main(String[] args) {

        LogConfig.init(Level.INFO);

        Registry reg = new Registry(Integer.parseInt(args[0]));
        new Thread(reg::startRegistry).start();
        new Thread(reg::readTerminal).start();
    }
}
