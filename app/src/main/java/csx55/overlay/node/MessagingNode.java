package csx55.overlay.node;

import csx55.overlay.wireformats.Event;

import java.io.IOException;
import java.net.*;

import javax.imageio.IIOException;

public class MessagingNode implements Node {

    private ServerSocket SS;
    private Socket CS;
    private int SP;
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

    public void registerNode() {
        try{
            CS = new Socket(HOST, CP);
            System.out.println("Connected to registry...\n" + "Using port: " + CP);
            System.out.println("Server Port : " + CS.getPort() + "\n" + "Local Port: " + CS.getLocalPort());
        } catch(IOException e) {
            System.out.println("Exception while connecting to registry..." + e.getLocalizedMessage());
        }
    }

    public static void main(String[] args) {
        MessagingNode MN = new MessagingNode(args[0], Integer.parseInt(args[1]));
        MN.nodeStart();
        MN.registerNode();
    }
    
}
