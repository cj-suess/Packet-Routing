package csx55.overlay.node;

import csx55.overlay.transport.TCPSender;
import csx55.overlay.wireformats.Event;

public interface Node {

    void onEvent(Event event, TCPSender sender); // adding socket information so events can respond
    
}
