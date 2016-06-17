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
package io.fabric8.docker.client.utils;

import io.fabric8.docker.api.model.Callback;
import io.fabric8.docker.client.DockerStreamData;
import okio.BufferedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;

public class DockerStreamPumper implements Runnable, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputStreamReader.class);

    private final BufferedSource in;
    private final Callback<DockerStreamData, Void> callback;
    private final Callback<Boolean, Void> onFinish;
    private boolean keepReading = true;
    private Thread thread;

    public DockerStreamPumper(BufferedSource in, Callback<DockerStreamData, Void> callback) {
        this(in, callback, new Callback<Boolean, Void>() {
            @Override
            public Void call(Boolean input) {
                return null;
            }
        });
    }

    public DockerStreamPumper(BufferedSource in, Callback<DockerStreamData, Void> callback, Callback<Boolean, Void> onFinish) {
        this.in = in;
        this.callback = callback;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        thread = Thread.currentThread();
        try {
            while (keepReading && !Thread.currentThread().isInterrupted()) {
                byte streamTypeByte = in.readByte();

                DockerStreamData.StreamType stream = DockerStreamData.StreamType.lookup(streamTypeByte);

                in.skip(3);

                byte[] sizeBytes = in.readByteArray(4);

                int size = ByteBuffer.wrap(sizeBytes).getInt();

                byte[] payload = in.readByteArray(size);

                callback.call(
                    new DockerStreamDataImpl(stream, size, payload)
                );
            }
            //To indicate that the response has been fully read.
            onFinish.call(true);
        } catch (InterruptedIOException e) {
            LOGGER.debug("Interrupted while pumping stream.", e);
            onFinish.call(false);
        } catch (IOException e) {
            onFinish.call(false);
            if (!Thread.currentThread().isInterrupted()) {
                LOGGER.error("Error while pumping stream.", e);
            } else {
                LOGGER.debug("Interrupted while pumping stream.", e);
            }
        }
    }

    public void close() {
        keepReading = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    private static class DockerStreamDataImpl implements DockerStreamData {

        private StreamType type;

        private int size;

        private byte[] payload;

        private DockerStreamDataImpl(StreamType type, int size, byte[] payload) {
            this.type = type;
            this.size = size;
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
    }
}
