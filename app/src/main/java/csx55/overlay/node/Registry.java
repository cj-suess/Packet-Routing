package csx55.overlay.node;

import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

import csx55.overlay.transport.*;
import csx55.overlay.wireformats.*;

public class Registry implements Node {

    public int port;
    public ServerSocket serverSocket;

    ConcurrentHashMap<String, Integer> nodeMap;


    public Registry(int port) {
        this.port = port;
        nodeMap = new ConcurrentHashMap<>();
    }

    public void onEvent(Event event) {

        // if Event == Register_Request
        if(event.getType() == Protocol.REGISTER_REQUEST) {
            Register node = (Register) event;
            // perform checks
            if(nodeMap.get(node.ip) == null) {
                // add to the map and respond with success and number of nodes in the map
                System.out.println("Adding node to map...");
                nodeMap.put(node.ip, node.port);
            } else if(nodeMap.get(node.ip) == node.port) {
                // respond with failure due to existing duplicate entry
            } 
                // add another check for node.ip matching ip of request?
        }
        printRegistry();
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
        System.out.println(nodeMap.size());
        nodeMap.forEach((key, value) -> System.out.println(key+":"+value));
    }

    public static void main(String[] args) {
        Registry reg = new Registry(Integer.parseInt(args[0]));
        new Thread(reg::startRegistry).start();
    }
}
