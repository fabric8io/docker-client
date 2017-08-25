package io.fabric8.docker.client.impl;

import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.DockerStreamData;
import io.fabric8.docker.client.utils.DockerStreamPumper;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputErrorHandle;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerLogHandle implements Callback, OutputErrorHandle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerLogHandle.class);

    private final OutputStream out;
    private final OutputStream err;

    private final PipedInputStream pipedOutput;
    private final PipedInputStream pipedError;
    private final EventListener listener;

    private final AtomicReference<Response> response = new AtomicReference<>();
    private final AtomicReference<Throwable> error = new AtomicReference<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DockerStreamPumper pumper;
    private final AtomicBoolean succeded = new AtomicBoolean(false);
    private final AtomicBoolean failed = new AtomicBoolean(false);

    private final CountDownLatch latch = new CountDownLatch(1);

    public ContainerLogHandle(OutputStream out, OutputStream err, PipedInputStream outputPipe,
        PipedInputStream errorPipe) {
        this(out, err, outputPipe, errorPipe, OperationSupport.NULL_LISTENER);
    }

    public ContainerLogHandle(OutputStream out, OutputStream err, PipedInputStream outputPipe, PipedInputStream errorPipe,
        EventListener listener) {

        this.out = outputStreamOrPipe(out, outputPipe);
        this.err = outputStreamOrPipe(err, errorPipe);

        this.pipedOutput = outputPipe;
        this.pipedError = errorPipe;

        this.listener = listener;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        error.set(e);
        listener.onError(e);
        latch.countDown();
    }

    @Override
    public void onResponse(Call call, Response r) throws IOException {
        response.set(r);

        if (out instanceof PipedOutputStream && pipedOutput != null) {
            pipedOutput.connect((PipedOutputStream) out);
        }

        pumper =
            new DockerStreamPumper(r.body().source(),
                    new io.fabric8.docker.api.model.Callback<DockerStreamData, Void>() {
                        @Override
                        public Void call(DockerStreamData input) {
                            processStream(input);
                            writeSteam(input);
                            return null;
                        }
                    }, new Runnable() {
                @Override
                public void run() {
                    if (succeded.compareAndSet(false, true) && !failed.get()) {
                        listener.onSuccess("Done.");
                    }
                }
            }, new io.fabric8.docker.api.model.Callback<Throwable, Void>() {
                @Override
                public Void call(Throwable t) {
                    if (failed.compareAndSet(false, true)) {
                        listener.onError(t);
                    }
                    return null;
                }
            });
        executorService.submit(pumper);
        latch.countDown();
    }

    private void writeSteam(DockerStreamData input) {
        if (input != null) {
            try {
                switch (input.streamType()) {
                    case STDOUT:
                    case RAW:
                        if (out != null) {
                            out.write(input.payload());
                            out.flush();
                        }
                        break;
                    case STDERR:
                        if (err != null) {
                            err.write(input.payload());
                            err.flush();
                        }
                        break;
                    default:
                        LOGGER.error("unknown stream type:" + input.streamType());
                }
            } catch (IOException e) {
                throw DockerClientException.launderThrowable(e);
            }
            LOGGER.debug(input.toString());
        }
    }

    private void processStream(DockerStreamData input) {
        if (input == null) {
            // ignore
        } else if (isFailure(input) && failed.compareAndSet(false, true)) {
            listener.onError("");
        } else {
            if (isSuccess(input) && succeded.compareAndSet(false, true)) {
                listener.onSuccess(input.toString());
            } else {
                listener.onEvent(input.toString());
            }
        }
    }

    private boolean isSuccess(DockerStreamData input) {
        return false;
    }

    private boolean isFailure(DockerStreamData input) {
        return false;
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

    @Override
    public void close() throws IOException {
        pumper.close();
        executorService.shutdown();

        try {
            if (executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (Throwable t) {
            throw DockerClientException.launderThrowable(t);
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

    @Override
    public InputStream getError() {
        return pipedError;
    }

    @Override
    public InputStream getOutput() {
        return pipedOutput;
    }
}
