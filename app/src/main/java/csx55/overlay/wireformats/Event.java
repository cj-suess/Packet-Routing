package csx55.overlay.wireformats;

public interface Event {

    private int getType() {
        return 0;
    }

    private byte[] getBytes() {
        return new byte[1];
    }
    
}
