package io.fabric8.docker.client.impl;

import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketListener;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.OutputHandle;
import okio.Buffer;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ContainerOutputHandle implements OutputHandle, WebSocketListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerOutputHandle.class);

    private final OutputStream out;
    private final OutputStream err;

    private final PipedInputStream output;
    private final PipedInputStream error;
    private final AtomicBoolean started = new AtomicBoolean(false);

    protected final AtomicReference<WebSocket> webSocketRef = new AtomicReference<>();
    protected final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(1);

    public ContainerOutputHandle(OutputStream out, OutputStream err, PipedInputStream outputPipe, PipedInputStream errorPipe) {
        this.out = outputStreamOrPipe(out, outputPipe);
        this.err = outputStreamOrPipe(err, errorPipe);

        this.output = outputPipe;
        this.error = errorPipe;
    }

    @Override
    public void close() {
        WebSocket ws = webSocketRef.get();
        try {
            if (ws != null) {
                ws.close(1000, "Closing...");
            }
        } catch (Throwable t) {
            throw DockerClientException.launderThrowable(t);
        }
    }

    public void waitUntilReady() {
        try {
            Object obj = queue.poll(10, TimeUnit.SECONDS);
            if (obj instanceof Boolean && ((Boolean) obj)) {
                return;
            } else {
                if (obj instanceof Throwable) {
                    throw (Throwable) obj;
                }
            }
        } catch (Throwable t) {
            throw DockerClientException.launderThrowable(t);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        try {
            if (out instanceof PipedOutputStream && output != null) {
                output.connect((PipedOutputStream) out);
            }
            if (err instanceof PipedOutputStream && error != null) {
                error.connect((PipedOutputStream) err);
            }

            webSocketRef.set(webSocket);
            started.set(true);
            queue.add(true);
        } catch (IOException e) {
            queue.add(e);
        }
    }

    @Override
    public void onFailure(IOException ioe, Response response) {
        LOGGER.error(response != null ? response.message() : "Exec Failure.", ioe);
        //We only need to queue startup failures.
        if (!started.get()) {
            queue.add(ioe);
        }
    }

    @Override
    public void onMessage(ResponseBody message) throws IOException {
        try {
            byte streamID = message.source().readByte();
            ByteString byteString = message.source().readByteString();
            if (byteString.size() > 0) {
                switch (streamID) {
                    case 1:
                        if (out != null) {
                            out.write(byteString.toByteArray());
                        }
                        break;
                    case 2:
                        if (err != null) {
                            err.write(byteString.toByteArray());
                        }
                        break;
                    case 3:
                        if (err != null) {
                            err.write(byteString.toByteArray());
                        }
                        break;
                    default:
                        throw new IOException("Unknown stream ID " + streamID);
                }
            }
        } finally {
            message.close();
        }
    }

    @Override
    public void onPong(Buffer buffer) {
        LOGGER.debug("Exec Web Socket: On Pong");
    }

    @Override
    public void onClose(int i, String s) {
        LOGGER.debug("Exec Web Socket: On Close");
    }

    public InputStream getOutput() {
        return output;
    }

    public InputStream getError() {
        return error;
    }

    private void send(byte[] bytes) throws IOException {
        if (bytes.length > 0) {
            WebSocket ws = webSocketRef.get();
            if (ws != null) {
                byte[] toSend = new byte[bytes.length + 1];
                toSend[0] = 0;
                System.arraycopy(bytes, 0, toSend, 1, bytes.length);
                ws.sendMessage(RequestBody.create(WebSocket.BINARY, toSend));
            }
        }
    }

    private static OutputStream outputStreamOrPipe(OutputStream stream, PipedInputStream in) {
        if (stream != null) {
            return stream;
        } else if (in != null) {
            return new PipedOutputStream();
        } else {
            return null;
        }
    }
}
