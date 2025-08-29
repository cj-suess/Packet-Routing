package csx55.overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerThread implements Runnable {

    public Socket socket;

    public TCPServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("New TCP Connection thread created...");
            System.out.println("Local Port: " + socket.getLocalPort());
            System.out.println("Remote Port: " + socket.getPort() + "\n");
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

}
