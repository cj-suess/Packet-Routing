package csx55.overlay.wireformats;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import csx55.overlay.util.Tuple;
import java.util.logging.*;

public class EventFactory {

    private final byte[] data;

    private static final Logger log = Logger.getLogger(EventFactory.class.getName());

    public EventFactory(byte[] data) {
        this.data = data;
    }

    public Event createEvent() {

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data); DataInputStream dis = new DataInputStream(bais)) {

            int messageType = dis.readInt();
            String ip;
            int port;
            int numConnections;

            log.info("New event being created...");

            switch (messageType) {
                case Protocol.REGISTER_REQUEST:
                    // decode data into Register event
                    log.info("\tDecoding data into a Register object...");
                    ip = readString(dis);
                    port = dis.readInt();
                    Register register_request = new Register(messageType, ip, port);
                    return register_request;
                case Protocol.DEREGISTER_REQUEST:
                    // decode into Deregister event
                    log.info("\tDecoding data into a Deregister object...");
                    ip = readString(dis);
                    port = dis.readInt();
                    Deregister deregister_request = new Deregister(messageType, ip, port);
                    return deregister_request;
                case Protocol.REGISTER_RESPONSE:
                case Protocol.DEREGISTER_RESPONSE:
                case Protocol.NODE_ID:
                    log.info("\tDecoding data into a Message object...");
                    return readStatusMessage(messageType, dis);
                case Protocol.MESSAGING_NODES_LIST:
                    // decode data into MessagingNodesList event
                    log.info("\tDecoding data into a MessagingNodesList object...");
                    numConnections = dis.readInt();
                    List<Tuple> peers = readPeers(dis, numConnections);
                    MessagingNodesList node_list = new MessagingNodesList(messageType, numConnections, peers);
                    return node_list;
                case Protocol.OVERLAY:
                    // decode data into Overlay event
                    log.info("\tDecoding data into a Overlay object...");
                    Map<String, List<Tuple>> overlay = new HashMap<>();
                    int numNodes = dis.readInt();
                    numConnections = dis.readInt();
                    for(int i = 0; i < numNodes; i++) {
                        String id = readString(dis);
                        overlay.put(id, readPeers(dis, numConnections));
                    }
                    Overlay overlayMessage = new Overlay(messageType, numNodes, numConnections, overlay);
                    return overlayMessage;
                case Protocol.LINK_WEIGHTS:
                    int dummyData = dis.readInt();
                    LinkWeights lw = new LinkWeights(messageType, dummyData);
                    return lw;
                case Protocol.TASK_INITIATE:
                    // decode data into TaskInitiate event
                    log.info("\tDecoding data into a TaskInitiate object....");
                    int numRounds = dis.readInt();
                    TaskInitiate ti = new TaskInitiate(messageType, numRounds);
                    return ti;
                case Protocol.TASK_COMPLETE:
                    // decode data into TaskComplete event
                    log.info("\tDecoding data into a TaskComplete object...");
                    ip = readString(dis);
                    port = dis.readInt();
                    TaskComplete taskComplete = new TaskComplete(messageType, ip, port);
                    return taskComplete;
                case Protocol.PULL_TRAFFIC_SUMMARY:
                    // decode data into TaskSummaryRequest event
                    log.info("\tDecoding data into a TaskSummaryRequest object...");
                    TaskSummaryRequest summaryRequest = new TaskSummaryRequest(messageType);
                    return summaryRequest;
                case Protocol.PAYLOAD:
                    // decode event into Payload object
                    log.info("\tDecoding data into a Payload object...");
                    int randNum = dis.readInt();
                    int pathSize = dis.readInt();
                    Queue<String> path = readPath(dis, pathSize);
                    Payload payload = new Payload(messageType, randNum, path);
                    return payload;
                case Protocol.TRAFFIC_SUMMARY:
                    // decode event into TaskSummaryResponse object
                    log.info("\tDecoding data into a TaskSummaryResponse object...");
                    int sendTracker = dis.readInt();
                    int receiveTracker = dis.readInt();
                    long sendSummation = dis.readLong();
                    long receiveSummation = dis.readLong();
                    int relayTracker = dis.readInt();
                    TaskSummaryResponse summaryResponse = new TaskSummaryResponse(messageType, sendTracker, receiveTracker, sendSummation, receiveSummation, relayTracker);
                    return summaryResponse;
                default:
                    log.warning("Unknown protocol passed to EventFactory...");
            }
        } catch(IOException e) {
            log.info("Exception while decoding data...");
        }
        return null;
    }

    private static Queue<String> readPath(DataInputStream dis, int pathSize) throws IOException {
        Queue<String> path = new LinkedList<>();
        for(int i = 0; i < pathSize; i++) {
            path.add(readString(dis));
        }
        return path;
    }

    private static Tuple createPeer(DataInputStream dis) throws IOException {
        String info = readString(dis);
        int weight = dis.readInt();
        return new Tuple(info, weight);
    }

    private static String readString(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        byte[] bytes = new byte[length];
        dis.readFully(bytes);
        return new String(bytes);
    }

    private static Message readStatusMessage(int messageType, DataInputStream dis) throws IOException {
        byte statusCode = dis.readByte();
        String info = readString(dis);
        return new Message(messageType, statusCode, info);
    }

    private static List<Tuple> readPeers(DataInputStream dis, int numConnections) throws IOException {
        List<Tuple> peers = new ArrayList<>();
        for(int i = 0; i < numConnections; i++) {
            peers.add(createPeer(dis));
        }
        return peers;
    }

}
