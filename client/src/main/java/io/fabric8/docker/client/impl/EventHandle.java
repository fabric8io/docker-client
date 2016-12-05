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

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import io.fabric8.docker.api.model.Callback;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.client.ProgressEvent;
import io.fabric8.docker.client.utils.InputStreamPumper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class EventHandle implements OutputHandle, okhttp3.Callback {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandle.class);

    private final long timeoutMillis;

    private final OutputStream out;
    private final PipedInputStream pin;
    private final EventListener listener;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final AtomicReference<Response> response = new AtomicReference<>();
    private final AtomicReference<Throwable> error = new AtomicReference<>();

    private final CountDownLatch latch = new CountDownLatch(1);
    private final Set<Closeable> closeables = new HashSet<>();

    private final AtomicBoolean succeded = new AtomicBoolean(false);
    private final AtomicBoolean failed = new AtomicBoolean(false);

    public EventHandle(OutputStream out, long duration, TimeUnit unit) {
        this(out, duration, unit, OperationSupport.NULL_LISTENER);
    }

    public EventHandle(OutputStream out, long duration, TimeUnit unit, EventListener listener) {
        this(out, unit.toMillis(duration), listener);
    }

    public EventHandle(OutputStream out, long timeoutMillis, EventListener listener) {
        this.out = out;
        this.timeoutMillis = timeoutMillis;
        this.listener = listener;

        if (out instanceof PipedOutputStream) {
            try {
                this.pin = new PipedInputStream();
                this.pin.connect((PipedOutputStream) out);
            } catch (IOException e) {
                throw DockerClientException.launderThrowable(e);
            }
        } else {
            pin = null;
        }
    }


    public boolean isSuccess(ProgressEvent event) {
        return false;
    }

    public boolean isFailure(ProgressEvent event) {
        return false;
    }


    @Override
    public void onFailure(Call call, IOException e) {
        error.set(e);
        listener.onError(e.getMessage());
        latch.countDown();
    }

    @Override
    public void onResponse(Call call, Response r) throws IOException {
        response.set(r);
        if (r.code() == 200) {
            InputStreamPumper pumper = new InputStreamPumper(r.body().byteStream(), new Callback<byte[], Void>() {
                @Override
                public Void call(byte[] data) {
                    onEvent(new String(data));
                    return null;
                }
            }, new Callback<Boolean, Void>() {
                @Override
                public Void call(Boolean success) {
                    if (success) {
                        if (succeded.compareAndSet(false, true) && !failed.get()) {
                            listener.onSuccess("Done.");
                        }
                    } else {
                        if (failed.compareAndSet(false, true)) {
                            listener.onError("Failed.");
                        }
                    }
                    return null;
                }
            });
            closeables.add(pumper);
            executorService.submit(pumper);
        } else {
            onFailure(call, new IOException(r.body().string()));
        }
        latch.countDown();
    }

    private void onEvent(String line) {
        ProgressEvent event = null;
        try {
            event = OperationSupport.JSON_MAPPER.readValue(line, ProgressEvent.class);
            if (event == null) {
                //ignore
            } else if (isFailure(event) && failed.compareAndSet(false, true)) {
                String error = event.getError();
                listener.onError(error);
            } else  {
                if (isSuccess(event) && succeded.compareAndSet(false, true)) {
                    listener.onSuccess(event.toString());
                } else {
                    listener.onEvent(event.toString());
                }
            }
        } catch (IOException t) {
            LOGGER.debug("Error while handling event.", t);
        } finally {
            if (event != null && out != null) {
                try {
                    out.write(event.toString().getBytes());
                } catch (IOException e) {
                    LOGGER.debug("Error while writing event to output stream.", e);
                }
            }
        }
    }

    @Override
    public InputStream getOutput() {
        try {
            if (latch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                Throwable t = error.get();
                Response r = response.get();

                if (t != null) {
                    throw DockerClientException.launderThrowable(t);
                } else if (r == null) {
                    throw new DockerClientException("Response not available");
                } else if (!r.isSuccessful()) {
                    throw new DockerClientException(r.message());
                } else if (pin == null) {
                    throw new DockerClientException("InputStream not available. Have you used redirectingOutput()?");
                } else {
                    return pin;
                }
            } else {
                throw new DockerClientException("Timed out waiting for response");
            }
        } catch (InterruptedException e) {
            try {
                close();
            } catch (IOException ioe) {
                throw DockerClientException.launderThrowable(e);
            } finally {
                Thread.currentThread().interrupt();
            }
        }
        throw new DockerClientException("Could not obtain stream");
    }

    @Override
    public void close() throws IOException {
        for (Closeable c : closeables) {
            try {
                c.close();
            } catch (IOException e) {
                LOGGER.warn("Error while closing stream pumper:" + e.getMessage());
            }
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        Response r = response.get();
        if (r != null) {
            try {
                r.body().close();
            } catch (Throwable t) {
                LOGGER.warn("Error while closing response stream:" + t.getMessage());
            }
        }
    }
}
