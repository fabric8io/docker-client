package io.fabric8.docker.client;

public interface DockerStreamData {

    StreamType streamType();

    int size();

    byte[] payload();

    enum StreamType {
        STDIN(0),
        STDOUT(1),
        STDERR(2),
        RAW(3);

        private final int value;

        StreamType(int streamType) {
            this.value = streamType;
        }
    }
}
