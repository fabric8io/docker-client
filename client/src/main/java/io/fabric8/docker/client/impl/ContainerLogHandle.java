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

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.InputStreamPumper;
import io.fabric8.docker.dsl.OutputErrorHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.arraycopy;

public class ContainerLogHandle implements OutputErrorHandle, Callback {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerLogHandle.class);

    private final OutputStream out;
    private final OutputStream err;

    private final PipedInputStream output;
    private final PipedInputStream error;
    private final AtomicBoolean started = new AtomicBoolean(false);

    protected final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(1);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private InputStreamPumper pumper;

    public ContainerLogHandle(OutputStream out, OutputStream err, PipedInputStream outputPipe, PipedInputStream errorPipe) {
        this.out = outputStreamOrPipe(out, outputPipe);
        this.err = outputStreamOrPipe(err, errorPipe);

        this.output = outputPipe;
        this.error = errorPipe;
    }

    @Override
    public void close() {
        executorService.shutdown();

        try {
            if (executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
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
    public void onFailure(Request request, IOException ioe) {
        LOGGER.error("Request Failure.", ioe);
        //We only need to queue startup failures.
        if (!started.get()) {
            queue.add(ioe);
        }
    }

    @Override
    public void onResponse(Response response) throws IOException {
        if (out instanceof PipedOutputStream && output != null) {
            output.connect((PipedOutputStream) out);
        }

        pumper = new InputStreamPumper(response.body().byteStream(), new io.fabric8.docker.api.model.Callback<byte[], Void>() {

            private Header header = null;

            @Override
            public Void call(byte[] input) {
                try {
                    int offset = 0;
                    if (header == null || header.remaining() <= 0) {
                        byte[] headerBytes = Arrays.copyOf(input, 8);
                        header = new Header(headerBytes);
                        offset = 8;
                    }

                    input = Arrays.copyOfRange(input, offset, header.remaining() + offset);
                    header.read(input.length);

                    switch (header.stream()) {
                        case 0:
                        case 1:
                            if (out != null) {
                                out.write(input);
                            }
                            break;
                        case 2:
                            if (err != null) {
                                err.write(input);
                            }
                            break;
                        default:
                            throw new IOException("Unknown stream ID " + header.stream());
                    }
                } catch (IOException e) {
                    throw DockerClientException.launderThrowable(e);
                }
                return null;
            }
        });
        executorService.submit(pumper);
        started.set(true);
        queue.add(true);
    }


    public InputStream getOutput() {
        return output;
    }

    public InputStream getError() {
        return error;
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

    private class Header {
        private byte stream;

        private int remaining;

        Header(byte[] header) {
            if (header.length < 8) {
                throw new IllegalArgumentException("Header bytes is too short");
            }

            stream = header[0];

            byte[] sizeBytes = new byte[4];
            arraycopy(header, 4, sizeBytes, 0, 4);

            remaining = ByteBuffer.wrap(sizeBytes).getInt();
        }

        public int remaining() {
            return remaining;
        }

        public void read(int n) {
            remaining -= n;
        }

        public byte stream() {
            return stream;
        }
    }
}
