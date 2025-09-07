package csx55.overlay.util;

public class Tuple {

    String endpoint;
    int weight;

    public Tuple(String endpoint, int weight) {
        this.endpoint = endpoint;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return endpoint + ", " + weight;
    }

    public void add(String string) {
        endpoint = string;
    }

    public String getEndpoint(){
        return endpoint;
    }

    public int getWeight() {
        return weight;
    }

    public String getIp() {
        return endpoint.substring(0, endpoint.indexOf(":"));
    }

    public String getPort() {
        return endpoint.substring(endpoint.indexOf(":") + 1);
    }
}
