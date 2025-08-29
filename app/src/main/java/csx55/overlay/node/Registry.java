package csx55.overlay.node;

import java.net.*;
import java.io.*;
import csx55.overlay.wireformats.Event;

public class Registry implements Node {

    private ServerSocket SS;
    private int PORT;

    public Registry(int PORT) {
        this.PORT = PORT;
    }

    public void onEvent(Event event) {

    }

    public void nodeStart() {
        try {
            SS = new ServerSocket(PORT); // take from stdin
            PORT = SS.getLocalPort();
            System.out.println("Registry is up and running. Listening on port: " + PORT + "\n\n");
            
            while(true) {
                Socket socket = SS.accept();
                System.out.println("New messaging node connected...\n" + "Local Port: " + socket.getLocalPort() + "\n" + "Remote Port: " + socket.getPort());
                NodeHandler NH = new NodeHandler(socket);
                new Thread(NH).start();
            }

        } catch(IOException e) {
            System.out.println(e.getMessage());
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
                System.out.println("Messaging node running in registry thread...\n");
            } catch(Exception e) {
                System.out.println("Exception with messaging node in registry thread... " + e.getLocalizedMessage());
            }
        }
    }

    public static void main(String[] args) {
        Registry reg = new Registry(Integer.parseInt(args[0]));
        reg.nodeStart();
    }
}
