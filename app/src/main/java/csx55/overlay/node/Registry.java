package csx55.overlay.node;

public class Registry implements Node {

    int id;

    public Registry(int id) {
        this.id = id;
    }

    public void printRegistry() {
        System.out.println("REGISTRY");
    }
}
