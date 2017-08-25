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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.Arrays;

public class InputStreamPumper implements Runnable, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputStreamReader.class);

    private final InputStream in;
    private final Callback<byte[], Void> callback;
    private final Runnable onSuccess;
    private final Callback<Throwable, Void> onError;
    private boolean keepReading = true;
    private Thread thread;

    public InputStreamPumper(InputStream in, Callback<byte[], Void> callback) {
        this(in, callback, new Runnable() {
            @Override
            public void run() {

            }
        }, new Callback<Throwable, Void>() {
            @Override
            public Void call(Throwable input) {
                return null;
            }
        });
    }

    public InputStreamPumper(InputStream in, Callback<byte[], Void> callback, Runnable onSuccess, Callback<Throwable, Void> onError) {
        this.in = in;
        this.callback = callback;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    @Override
    public void run() {
        thread = Thread.currentThread();
        byte[] buffer = new byte[1024];
        try {
            int length;
            while (keepReading && !Thread.currentThread().isInterrupted() && (length = in.read(buffer)) != -1) {
                byte[] actual = Arrays.copyOf(buffer, length);
                callback.call(actual);
            }
            //To indicate that the response has been fully read.
            onSuccess.run();
        } catch (InterruptedIOException e) {
            LOGGER.debug("Interrupted while pumping stream.", e);
            onError.call(e);
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted()) {
                LOGGER.error("Error while pumping stream.", e);
            } else {
                LOGGER.debug("Interrupted while pumping stream.", e);
            }
            onError.call(e);
        }
    }

    public void close() {
        keepReading = false;
        if (thread != null) {
            thread.interrupt();
        }
    }
}
