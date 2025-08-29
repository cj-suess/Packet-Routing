package csx55.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class TCPReceiverThread implements Runnable {

    private Socket socket;
    private DataInputStream din;

    public TCPReceiverThread(Socket socket) throws IOException {
        this.socket = socket;
        din = new DataInputStream(socket.getInputStream());
    }

    public void run() {
        int dataLength;
        while(socket != null) {
            try {
                dataLength = din.readInt();
                byte[] data = new byte[dataLength];
                din.readFully(data, 0, dataLength);
            } catch(SocketException soe) {
                System.out.println("Socket exception caught reading data..." + soe.getMessage());
                break;
            } catch(IOException ioe) {
                System.out.println("IO exception caught reading data..." + ioe.getMessage());
            }
        }

    }
    
}
