/*
 * Copyright (C) 2016 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.fabric8.docker.client.impl;

import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.OutputErrorHandle;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ContainerOutputHandle extends WebSocketListener implements OutputErrorHandle  {

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
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        LOGGER.error(response != null ? response.message() : "Exec Failure.", t);
        //We only need to queue startup failures.
        if (!started.get()) {
            queue.add(t);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            byte streamID = (text.getBytes())[0];   // read the first byte
            ByteString byteString = ByteString.encodeString(text, Charset.defaultCharset());
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getOutput() {
        return output;
    }

    public InputStream getError() {
        return error;
    }

    protected void send(byte[] bytes) throws IOException {
        if (bytes.length > 0) {
            WebSocket ws = webSocketRef.get();
            if (ws != null) {
                byte[] toSend = new byte[bytes.length + 1];
                toSend[0] = 0;
                System.arraycopy(bytes, 0, toSend, 1, bytes.length);
                ws.send(ByteString.of(toSend));
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
