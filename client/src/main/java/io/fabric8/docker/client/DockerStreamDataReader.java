package io.fabric8.docker.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.validation.constraints.NotNull;

import static io.fabric8.docker.client.DockerStreamData.StreamType.RAW;
import static io.fabric8.docker.client.DockerStreamData.StreamType.STDERR;
import static io.fabric8.docker.client.DockerStreamData.StreamType.STDIN;
import static io.fabric8.docker.client.DockerStreamData.StreamType.STDOUT;

public class DockerStreamDataReader implements AutoCloseable {

    private static final int HEADER_SIZE = 8;

    private final byte[] rawBuffer = new byte[1000];

    private final InputStream inputStream;

    private Boolean rawStreamDetected = false;

    public DockerStreamDataReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    private static DockerStreamData.StreamType streamType(byte streamType) {
        switch (streamType) {
            case 0:
                return STDIN;
            case 1:
                return STDOUT;
            case 2:
                return STDERR;
            default:
                return RAW;
        }
    }

    @NotNull
    public DockerStreamDataImpl readStreamData() throws IOException {

        if (rawStreamDetected) {
            int read = inputStream.read(rawBuffer);
            if (read == -1) {
                return null;
            }

            return new DockerStreamDataImpl(RAW, Arrays.copyOf(rawBuffer, read));
        } else {

            byte[] header = new byte[HEADER_SIZE];

            int actualHeaderSize = 0;

            do {
                int headerCount = inputStream.read(header, actualHeaderSize, HEADER_SIZE - actualHeaderSize);

                if (headerCount == -1) {
                    return null;
                }
                actualHeaderSize += headerCount;
            } while (actualHeaderSize < HEADER_SIZE);

            DockerStreamData.StreamType streamType = streamType(header[0]);

            if (streamType.equals(RAW)) {
                rawStreamDetected = true;
                return new DockerStreamDataImpl(RAW, Arrays.copyOf(header, HEADER_SIZE));
            }

            int payloadSize = ((header[4] & 0xff) << 24) + ((header[5] & 0xff) << 16) + ((header[6] & 0xff) << 8)
                + (header[7] & 0xff);

            byte[] payload = new byte[payloadSize];
            int actualPayloadSize = 0;

            do {
                int count = inputStream.read(payload, actualPayloadSize, payloadSize - actualPayloadSize);

                if (count == -1) {
                    if (actualPayloadSize != payloadSize) {
                        throw new IOException(String.format("payload must be %d bytes long, but was %d", payloadSize,
                            actualPayloadSize));
                    }
                    break;
                }
                actualPayloadSize += count;
            } while (actualPayloadSize < payloadSize);

            return new DockerStreamDataImpl(streamType, payload);
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
