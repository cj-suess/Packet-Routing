package csx55.overlay.wireformats;

public interface Protocol {
    int REGISTER_REQUEST = 0;
    int REGISTER_RESPONSE = 1;
    int DEREGISTER_REQUEST = 2;
    int DEREGISTER_RESPONSE = 3;
    int MESSAGING_NODES_LIST = 4;
    int LINK_WEIGHTS = 5;
    int TASK_INITIATE = 6;
    int TASK_COMPLETE = 7;
    int PULL_TRAFFIC_SUMMARY = 8;
    int TRAFFIC_SUMMARY = 9;
}
