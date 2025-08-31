package csx55.overlay.node;

import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

import csx55.overlay.transport.*;
import csx55.overlay.wireformats.*;

public class Registry implements Node {

    public int port;
    public ServerSocket serverSocket;

    Set<String> registeredNodes;


    public Registry(int port) {
        this.port = port;
        registeredNodes = ConcurrentHashMap.newKeySet(); // should work better using a single String?
    }

    public void onEvent(Event event) {
        if(event.getType() == Protocol.REGISTER_REQUEST) {
            Register node = (Register) event; // downcast back to Register
            String nodeEntry = node.ip + ":" + node.port;
            if(registeredNodes.add(nodeEntry)) {
                // change to message sent to messaging node
                System.out.printf("Registration request successful. The number of messaging nodes currently constituting the overlay is %d.", registeredNodes.size());
            } else {
                // change to message sent to messaging node
                System.err.println("[Error] A node with the same IP address and port already is registered...");
            }
        }
    }

    public void startRegistry() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Registry is up and running. Listening on port: " + port + "\n");

            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection from: " + socket.getInetAddress().getHostAddress());
                TCPServerThread st = new TCPServerThread(socket, this);
                new Thread(st).start();
            }

        } catch(IOException e) {
            System.out.println("Exception while starting registry node..." + e.getMessage());
        }
    }

    public void printRegistry() {
        System.out.println("Number of nodes currently in registry: " + registeredNodes.size());
        registeredNodes.forEach(key -> System.out.println(key));
    }

    public static void main(String[] args) {
        Registry reg = new Registry(Integer.parseInt(args[0]));
        new Thread(reg::startRegistry).start();
    }
}
