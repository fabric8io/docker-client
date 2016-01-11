package io.fabric8.docker.client.impl;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.fabric8.docker.api.model.Callback;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.client.ProgressEvent;
import io.fabric8.docker.client.utils.InputStreamPumper;
import io.fabric8.docker.client.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class EventHandle implements OutputHandle, com.squareup.okhttp.Callback {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandle.class);

    private final long timeoutMillis;
    private final PipedInputStream pin;
    private final PipedOutputStream pout;
    private final EventListener listener;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final AtomicReference<Response> response = new AtomicReference<>();
    private final AtomicReference<Throwable> error = new AtomicReference<>();

    private final CountDownLatch latch = new CountDownLatch(1);
    private final Set<Closeable> closeables = new HashSet<>();

    public EventHandle(long duration, TimeUnit unit) {
        this(duration, unit, OperationSupport.NULL_LISTENER);
    }

    public EventHandle(long duration, TimeUnit unit, EventListener listener) {
        this(unit.toMillis(duration), listener);
    }

    public EventHandle(long timeoutMillis, EventListener listener) {
        this.timeoutMillis = timeoutMillis;
        this.listener = listener;
        this.pin = new PipedInputStream();
        this.pout = new PipedOutputStream();
        try {
            this.pin.connect(pout);
        } catch (IOException e) {
            throw DockerClientException.launderThrowable(e);
        }
    }


    public boolean isSuccess(ProgressEvent event) {
        return false;
    }

    public boolean isFailure(ProgressEvent event) {
        return false;
    }

    @Override
    public void onFailure(Request request, IOException e) {
        error.set(e);
        listener.onError(e.getMessage());
        latch.countDown();
    }

    @Override
    public void onResponse(Response r) throws IOException {
        response.set(r);
        if (r.code() == 200) {
            InputStreamPumper pumper = new InputStreamPumper(r.body().byteStream(), new Callback<byte[], Void>() {
                @Override
                public Void call(byte[] data) {
                    onEvent(new String(data));
                    return null;
                }
            });
            closeables.add(pumper);
            executorService.submit(pumper);
        } else {
            onFailure(r.request(), new IOException(r.body().string()));
        }
        latch.countDown();
    }

    private void onEvent(String line) {
        try {
            ProgressEvent event = OperationSupport.JSON_MAPPER.readValue(line, ProgressEvent.class);
            if (event == null) {
                //ignore
            } else if (Utils.isNotNullOrEmpty(event.getStream())) {
                String stream = event.getStream();
                if (isSuccess(event)) {
                    listener.onSuccess(stream);
                } else {
                    listener.onEvent(stream);
                }
                pout.write(stream.getBytes());
            } else if (isFailure(event)) {
                String error = event.getError();
                pout.write(error.getBytes());
                listener.onError(error);
            }
        } catch (IOException t) {
            LOGGER.debug("Error while handling event.", t);
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
                r.body().source().close();
            } catch (Throwable t) {
                LOGGER.warn("Error while closing response stream:" + t.getMessage());
            }
        }
    }
}
