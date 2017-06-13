package io.fabric8.docker.client;

public class DockerStreamDataImpl implements DockerStreamData {

    private StreamType type;

    private int size;

    private byte[] payload;

    public DockerStreamDataImpl(StreamType type, int size, byte[] payload) {
        this.type = type;
        this.size = size;
        this.payload = payload;
    }

    public DockerStreamDataImpl(StreamType type, byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    @Override
    public StreamType streamType() {
        return type;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public byte[] payload() {
        return payload;
    }

    @Override
    public String toString() {
        return new String(payload).trim();
    }
}

