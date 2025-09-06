package csx55.overlay.node;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;

import csx55.overlay.transport.*;
import csx55.overlay.util.OverlayCreator;
import csx55.overlay.util.Tuple;
import csx55.overlay.wireformats.*;

public class Registry implements Node {

    public int port;
    public ServerSocket serverSocket;

    List<TCPConnection> openConnections;

    Set<String> registeredNodes;
    Map<String, List<Tuple>> overlay; // grab messaging node ip and match with correct socket in openConnections
    Map<String, List<Tuple>> connectionMap; // use for relaying who connects to who to avoid duplicate connections

    public Registry(int port) {
        this.port = port;
        registeredNodes = ConcurrentHashMap.newKeySet(); // should work better using a single String?
        openConnections =  new ArrayList<>();
    }

    public void onEvent(Event event, Socket socket) {

        TCPSender sender = getSender(openConnections, socket);
        String socketAddress = socket.getInetAddress().getHostAddress(); // messaging nodes IP address
        int socketPort = socket.getPort(); // messaging nodes port

        try {
            if(event.getType() == Protocol.REGISTER_REQUEST) {
                System.out.println("[Registry] Register request detected. Checking status...");
                Register node = (Register) event; // downcast back to Register
                String nodeEntry = node.ip + ":" + node.port;
                if(node.ip.equals(socketAddress) && registeredNodes.add(nodeEntry)) {
                    System.out.printf("[Registry] New node was added to the registry successfully!\n" + "[Registry] Current number of nodes in registry %d\n", registeredNodes.size());
                    String info = "Registration request successful. The number of messaging nodes currently constituting the overlay is (" + registeredNodes.size() + ")";
                    Message successMessage = new Message(Protocol.REGISTER_RESPONSE, (byte)0, info);
                    System.out.printf("[Registry] Sending success response to messaging node at %s\n", nodeEntry);
                    sender.sendData(successMessage.getBytes());
                } else {
                    // add failure cases
                    System.out.printf("[Registry] Failure ocurred while registering node...\n\tChecking for mismatching IPs...\n\tIP in message: %s Socket IP: %s\n", node.ip, socketAddress);
                    if(!node.ip.equals(socketAddress)) {
                        // send failure for mismatch
                        System.out.println("[Registry] Sending failure message for mismatching IPs...");
                        String info = "Registration request failed. The IP address in the registration request did not match the IP address of the socket.";
                        Message failureMessage = new Message(Protocol.REGISTER_RESPONSE, (byte)1, info);
                        sender.sendData(failureMessage.getBytes());
                    } else {
                        // send failure for duplicate
                        System.out.println("[Registry] Sending failure message for duplicate IPs...");
                        String info = "Registration request failed. The node entry already exists in the registry.";
                        Message failureMessage = new Message(Protocol.REGISTER_RESPONSE, (byte)1, info);
                        sender.sendData(failureMessage.getBytes());

                    }
                }
            }
            else if(event.getType() == Protocol.DEREGISTER_REQUEST) {
                System.out.println("[Registry] Deregister request detected. Checking status...");
                Deregister node = (Deregister) event; // downcast back to Deregister
                String nodeEntry = node.ip + ":" + node.port;
                if(registeredNodes.remove(nodeEntry) && node.ip.equals(socketAddress)) {
                    System.out.printf("[Registry] The node at %s has been removed from the registry...\n", nodeEntry);
                    String info = "The node has been successfully removed from the registry...";
                    Message successMessage = new Message(Protocol.DEREGISTER_RESPONSE, (byte)0, info);
                    System.out.printf("[Registry] Sending deregistration success response to messaging node at %s\n", nodeEntry);
                    sender.sendData(successMessage.getBytes());
                } else {
                    if(!node.ip.equals(socketAddress)){
                        // mismatch
                        System.out.println("[Registry] Sending failure message for mismatching IPs...");
                        String info = "Deregistration request failed. The IP address in the deregistration request did not match the IP address of the socket.";
                        Message failureMessage = new Message(Protocol.DEREGISTER_RESPONSE, (byte)1, info);
                        System.out.printf("[Registry] Sending deregistration failure response to messaging node at %s\n", nodeEntry);
                        sender.sendData(failureMessage.getBytes());
                    } else {
                        // node entry does not exist
                        System.out.printf("[Registry] The node %s does not exist in the registry...\n", nodeEntry);
                        String info = "The node could not be removed from the registry since it was not registered...";
                        Message failureMessage = new Message(Protocol.DEREGISTER_RESPONSE, (byte)1, info);
                        System.out.printf("[Registry] Sending deregistration failure response to messaging node at %s\n", nodeEntry);
                        sender.sendData(failureMessage.getBytes());
                    }
                }
            }
        } catch(IOException e) {
            System.err.println("[Registry] Exception in registery while handling an event...");
        }
    }

    private TCPSender getSender(List<TCPConnection> openConnections, Socket socket) {
        for(TCPConnection conn : openConnections) {
            if(socket == conn.socket) {
                return conn.sender;
            }
        }
        return null;
    }

    public void startRegistry() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[Registry] Registry is up and running. Listening on port: " + port + "\n");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> { // needed if the terminal crashes so the node deregisters. not sure if I can catch it elsewhere
                try {
                    serverSocket.close();
                } catch(IOException e) {
                    System.err.println("Exception while trying to clean up after sudden termination...");
                }
            }));

            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("\n[Registry] New connection from: " + socket.getInetAddress().getHostAddress());
                TCPConnection conn = new TCPConnection(socket, this); // possibly refactor? not sure if this is redundant 
                openConnections.add(conn);
                new Thread(conn).start();
            }

        } catch(IOException e) {
            System.out.println("[Registry] Exception while starting registry node..." + e.getMessage());
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
                        System.out.println("[Registry] Closing registry node...");
                        scanner.close();
                        System.exit(0);
                        break;
                    case "list-messaging-nodes":
                        printRegistry();
                        break;
                    case "setup-overlay":
                        int connections = 0;
                        if(splitCommand.length > 1) {
                            connections = Integer.parseInt(splitCommand[1]);
                        }
                        OverlayCreator oc = new OverlayCreator(registeredNodes, connections);
                        overlay = oc.build();
                        connectionMap = oc.filter();
                        // send connection instructions to messaging nodes
                        sendConnections();
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
                    default:
                        break;
                }
            }
        } catch(Exception e) {
            System.err.println("[Registry] Exception in terminal reader...");
        }
    }

    public void sendConnections() {
        System.out.println("Sending connections to the messaging nodes...");
        for(Map.Entry<String, List<Tuple>> entry : connectionMap.entrySet()) {
            
        }

    }

    public void printRegistry() {
        registeredNodes.forEach(key -> System.out.println(key));
    }

    public void printConnections() {
        for(TCPConnection conn : openConnections) {
            System.out.println(conn.socket.getInetAddress().getHostAddress());
        }
    }

    public void printOverlay() {
        for(Map.Entry<String, List<Tuple>> entry : overlay.entrySet()) {
            System.out.print("Messaging Node: " + entry.getKey() + " [Connected nodes:");
            for(Tuple t : entry.getValue()) {
                System.out.print(" " + t.getEndpoint());
            }
            System.out.println("]");
        }
    }

    public void printConnectionMap() {
        for(Map.Entry<String, List<Tuple>> entry : connectionMap.entrySet()){
            System.out.println(entry);
        }
    }

    public static void main(String[] args) {
        Registry reg = new Registry(Integer.parseInt(args[0]));
        new Thread(reg::startRegistry).start();
        new Thread(reg::readTerminal).start();
    }
}
