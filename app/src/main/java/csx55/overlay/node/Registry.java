package csx55.overlay.node;

import java.io.IOException;
import java.net.*;
import csx55.overlay.wireformats.Event;

public class Registry implements Node {

    private ServerSocket SS;
    private int PORT;

    public void onEvent(Event event) {

    }

    public void registerStart() {
        try {
            SS = new ServerSocket(0); // spin up registry with automatically configured port number
            PORT = SS.getLocalPort();
            System.out.println("Registry is up and running. Listening on port: " + PORT);
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
