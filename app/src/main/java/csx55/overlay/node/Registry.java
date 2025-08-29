package csx55.overlay.node;

import java.net.*;
import java.io.*;

import csx55.overlay.transport.TCPReceiverThread;
import csx55.overlay.transport.TCPSender;
import csx55.overlay.transport.TCPServerThread;
import csx55.overlay.wireformats.Event;

public class Registry implements Node {

    public int port;
    public ServerSocket serverSocket;
    TCPServerThread st;

    public Registry(int port) {
        this.port = port;
    }

    public void onEvent(Event event) {

        // if Event == Register_Request
        if(event.getType() == 0) {

        }
            // check if messaging node is registered and register if not
            // respond according to success/failure of registering
        // if Event == Deregister_Request
            // remove the messaging node from the registry
            // respond accordingly 

    }

    public void startRegistry() {
        try{
            serverSocket = new ServerSocket(port);
            System.out.println("Registry is up and running. Listening on port: " + port);

            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection from: " + socket.getInetAddress());
                st = new TCPServerThread(socket);
                new Thread(st).start();
            }
        } catch(IOException e) {
            System.out.println("Exception while starting registry node..." + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Registry reg = new Registry(Integer.parseInt(args[0]));
        reg.startRegistry();
    }
}
