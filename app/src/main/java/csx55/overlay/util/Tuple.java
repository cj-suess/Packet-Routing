package csx55.overlay.util;

public class Tuple {

    String endpoint;
    Integer weight;

    public Tuple(String endpoint, Integer weight) {
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
}
