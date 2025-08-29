package csx55.overlay.wireformats;

public interface Protocol {
    int REGISTER_REQUEST = 0;
    int REGISTER_RESPONSE = 1;
    int DEREGISTER_REQUEST = 2;
    int MESSAGING_NODES_LIST = 3;
    int LINK_WEIGHTS = 4;
    int TASK_INITIATE = 5;
    int TASK_COMPLETE = 6;
    int PULL_TRAFFIC_SUMMARY = 7;
    int TRAFFIC_SUMMARY = 8;
}
