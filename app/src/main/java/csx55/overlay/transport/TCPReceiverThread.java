package csx55.overlay.transport;

import csx55.overlay.node.Node;
import csx55.overlay.wireformats.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class TCPReceiverThread implements Runnable {

    private Socket socket;
    private DataInputStream din;
    private Node node;
    private TCPSender sender;

    public TCPReceiverThread(Socket socket, Node node, TCPSender sender) throws IOException {
        this.socket = socket;
        this.node = node;
        this.sender = sender;
        din = new DataInputStream(socket.getInputStream());
    }

    public void run() {
        int dataLength;
        while(socket != null) {
            try {
                dataLength = din.readInt();
                byte[] data = new byte[dataLength];
                din.readFully(data, 0, dataLength);
                // pass data to EventFactory
                EventFactory ef = new EventFactory(data);
                Event decodedEvent = ef.createEvent();
                node.onEvent(decodedEvent, sender);
            } catch(SocketException soe) {
                System.out.println("Socket exception caught reading data..." + soe.getMessage());
                break;
            } catch(IOException ioe) {
                System.out.println("IO exception caught reading data..." + ioe.getMessage());
                break;
            } 
        }

    }
    
}
