package csx55.overlay.wireformats;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import csx55.overlay.util.Tuple;
import java.util.logging.*;

public class EventFactory {

    byte[] data;
    int messageType;

    private Logger LOG = Logger.getLogger(EventFactory.class.getName());

    public EventFactory(byte[] data) {
        this.data = data;
    }

    public Event createEvent() {

        try {

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            messageType = dis.readInt();

            String ip;
            int port;
            int ipLength;
            byte[] ipBytes;

            byte statusCode;
            String info = null;
            int infoLength = 0;
            byte[] infoBytes = null;

            int numConnections;
            List<Tuple> peers = new ArrayList<>();
            int weight = 0;

            LOG.info("New event being created...");

            switch (messageType) {
                case Protocol.REGISTER_REQUEST:
                    // decode data into Register event
                    LOG.info("\tDecoding data into a Register object...");
                    ipLength = dis.readInt();
                    ipBytes = new byte[ipLength];
                    dis.readFully(ipBytes);
                    ip = new String(ipBytes);
                    port = dis.readInt();
                    bais.close();
                    dis.close();
                    Register register_request = new Register(messageType, ip, port);
                    return register_request;
                case Protocol.DEREGISTER_REQUEST:
                    // decode into Deregister event
                    LOG.info("\tDecoding data into a Deregister object...");
                    ipLength = dis.readInt();
                    ipBytes = new byte[ipLength];
                    dis.readFully(ipBytes);
                    ip = new String(ipBytes);
                    port = dis.readInt();
                    bais.close();
                    dis.close();
                    Deregister deregister_request = new Deregister(messageType, ip, port);
                    return deregister_request;
                case Protocol.REGISTER_RESPONSE:
                    // decode data into Message event
                    LOG.info("\tDecoding data into a Message object...");
                    statusCode = dis.readByte();
                    infoLength = dis.readInt();
                    infoBytes = new byte[infoLength];
                    dis.readFully(infoBytes);
                    info = new String(infoBytes);
                    bais.close();
                    dis.close();
                    Message register_response = new Message(messageType, statusCode, info);
                    return register_response;
                case Protocol.DEREGISTER_RESPONSE:
                    // decode data into Message event
                    LOG.info("\tDecoding data into a Message object...");
                    statusCode = dis.readByte();
                    infoLength = dis.readInt();
                    infoBytes = new byte[infoLength];
                    dis.readFully(infoBytes);
                    info = new String(infoBytes);
                    bais.close();
                    dis.close();
                    Message deregister_response = new Message(messageType, statusCode, info);
                    return deregister_response;
                case Protocol.MESSAGING_NODES_LIST:
                    // decode data into MessagingNodesList event
                    LOG.info("\tDecoding data into a MessagingNodesList object...");
                    numConnections = dis.readInt();
                    for(int i = 0; i < numConnections; i++) {
                        Tuple t = createPeer(dis, info, infoLength, infoBytes, weight);
                        peers.add(t);
                    }
                    bais.close();
                    dis.close();
                    MessagingNodesList node_list = new MessagingNodesList(messageType, numConnections, peers);
                    return node_list;
                case Protocol.NODE_ID:
                    // decode data into Message event
                    LOG.info("\tDecoding data into a Message object...");
                    statusCode = dis.readByte();
                    infoLength = dis.readInt();
                    infoBytes = new byte[infoLength];
                    dis.readFully(infoBytes);
                    info = new String(infoBytes);
                    bais.close();
                    dis.close();
                    Message idMessage = new Message(messageType, statusCode, info);
                    return idMessage;
                case Protocol.OVERLAY:
                    // decode data into Overlay event
                    LOG.info("\tDecoding data into a Overlay object...");
                    Map<String, List<Tuple>> overlay = new HashMap<>();
                    int numNodes = dis.readInt();
                    numConnections = dis.readInt();
                    for(int i = 0; i < numNodes; i++) {
                        int idLength = dis.readInt();
                        byte[] idBytes = new byte[idLength];
                        dis.readFully(idBytes);
                        String id = new String(idBytes);
                        List<Tuple> list = new ArrayList<>();
                        for(int j = 0; j < numConnections; j++) {
                            Tuple t = createPeer(dis, info, infoLength, infoBytes, weight);
                            list.add(t);
                        }
                        overlay.put(id, list);
                    }
                    bais.close();
                    dis.close();
                    Overlay overlayMessage = new Overlay(Protocol.OVERLAY, numNodes, numConnections, overlay);
                    return overlayMessage;
                case Protocol.LINK_WEIGHTS:
                    int dummyData = dis.readInt();
                    bais.close();
                    dis.close();
                    LinkWeights lw = new LinkWeights(messageType, dummyData);
                    return lw;
                default:
                    LOG.warning("Unknown protocol passed to EventFactory...");
            }
        } catch(IOException e) {
            LOG.info("Exception while decoding data...");
        }
        return null;
    }

    private Tuple createPeer(DataInputStream dis, String info, int infoLength, byte[] infoBytes, int weight) throws IOException {
        infoLength = dis.readInt();
        infoBytes = new byte[infoLength];
        dis.readFully(infoBytes);
        info = new String(infoBytes);
        weight = dis.readInt();
        return new Tuple(info, weight);
    }

}
