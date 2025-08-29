package csx55.overlay.node;

import csx55.overlay.wireformats.Event;
import java.io.IOException;
import java.net.*;

public class MessagingNode implements Node {

    private static ServerSocket SS;
    private Socket CS;
    private static int SP;
    private int CP;
    private String HOST;

    public MessagingNode(String HOST, int CP) {
        this.HOST = HOST;
        this.CP = CP;
    }

    public void onEvent(Event event) {

    }

    public void nodeStart() {
        try {
                SS = new ServerSocket(0); // spin up registry with automatically configured port number
                SP = SS.getLocalPort();
                System.out.println("Messaging node is up and running. Listening on port: " + SP);
                
                while(true) {
                    Socket socket = SS.accept();
                    System.out.println("New messaging node connected...\n" + "Local Port: " + socket.getLocalPort() + "\n" + "Remote Port: " + socket.getPort());
                }
            } catch(IOException e) {
                System.out.println(e.getMessage());
            }
    }

    // public void connectToMessagingNode(String hostname, int serverPort) {
    //     try{
    //         Socket clientSocket = new Socket(hostname, serverPort);
    //         System.out.println("Connected to other messaging node...\n" + "Using port: " + serverPort);
    //         System.out.println("Server Port : " + clientSocket.getPort() + "\n" + "Local Port: " + clientSocket.getLocalPort());
    //     } catch(IOException e) {
    //         System.out.println("Exception while connecting to other messaging node..." + e.getLocalizedMessage());
    //     }
    // }

    public void connectToRegistery() {
        try{
            CS = new Socket(HOST, CP);
            System.out.println("Connected to registry...\n" + "Using port: " + CP);
            System.out.println("Server Port : " + CS.getPort() + "\n" + "Local Port: " + CS.getLocalPort());
        } catch(IOException e) {
            System.out.println("Exception while connecting to registry..." + e.getLocalizedMessage());
        }
    }

    public class NodeHandler implements Runnable {
        
        private Socket CS;

        public NodeHandler(Socket CS) {
            this.CS = CS;
        }

        @Override
        public void run(){
            try {
                System.out.println("Messaging node running in messaging thread...");
            } catch(Exception e) {
                System.out.println("Exception with messaging node in messaging thread... " + e.getLocalizedMessage());
            }
        }

    }

    public static void main(String[] args) {
        MessagingNode MN = new MessagingNode(args[0], Integer.parseInt(args[1]));
        Thread thread = new Thread(MN::nodeStart);
        thread.start();
        MN.connectToRegistery();
    }
    
}
