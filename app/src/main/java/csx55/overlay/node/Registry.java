package csx55.overlay.node;

import java.net.*;
import java.io.*;

import csx55.overlay.transport.*;
import csx55.overlay.wireformats.*;

public class Registry implements Node {

    public int port;
    public ServerSocket serverSocket;


    public Registry(int port) {
        this.port = port;
    }

    public void onEvent(Event event) {

        // if Event == Register_Request
        if(event.getType() == Protocol.REGISTER_REQUEST) {
            // perform checks
            // add node if unregistered --> respond with success
            // respond with failure if already registered
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

    public static void main(String[] args) {
        Registry reg = new Registry(Integer.parseInt(args[0]));
        new Thread(reg::startRegistry).start();
    }
}
