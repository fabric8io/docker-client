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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ws.WebSocketCall;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.URLUtils;
import io.fabric8.docker.dsl.InputOutputErrorHandle;
import io.fabric8.docker.dsl.container.ContainerErrorStreamGetLogsInterface;
import io.fabric8.docker.dsl.container.ContainerInputOutputErrorStreamGetLogsInterface;
import io.fabric8.docker.dsl.container.ContainerOutputErrorStreamGetLogsInterface;
import io.fabric8.docker.dsl.container.StreamGetLogsInterface;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;

public class AttachContainer extends BaseContainerOperation implements
        ContainerInputOutputErrorStreamGetLogsInterface<InputOutputErrorHandle>,
        ContainerOutputErrorStreamGetLogsInterface<InputOutputErrorHandle>,
        ContainerErrorStreamGetLogsInterface<InputOutputErrorHandle>,
        StreamGetLogsInterface<InputOutputErrorHandle> {

    private static final String STDIN = "stdin";
    private static final String STDOUT = "stdout";
    private static final String STDERR = "stderr";
    private static final String STREAM = "stream";
    private static final String LOGS = "logs";

    private final InputStream in;
    private final OutputStream out;
    private final OutputStream err;

    private final PipedOutputStream inPipe;
    private final PipedInputStream outPipe;
    private final PipedInputStream errPipe;

    public AttachContainer(OkHttpClient client, Config config, String name, InputStream in, OutputStream out, OutputStream err, PipedOutputStream inPipe, PipedInputStream outPipe, PipedInputStream errPipe) {
        super(client, config, name, "attach");
        this.in = in;
        this.out = out;
        this.err = err;
        this.inPipe = inPipe;
        this.outPipe = outPipe;
        this.errPipe = errPipe;
    }

    private InputOutputErrorHandle doAttach(Boolean logs, Boolean stream) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(URLUtils.join(getOperationUrl().toString(), "ws"));

            sb.append("?").append(STREAM).append("=").append(true);
            sb.append("?").append(LOGS).append("=").append(true);
            sb.append("&").append(STDIN).append("=").append(in != null || inPipe != null);
            sb.append("&").append(STDOUT).append("=").append(out != null || outPipe != null);
            sb.append("&").append(STDERR).append("=").append(err != null || errPipe != null);
            Request.Builder r = new Request.Builder().url(sb.toString()).get();
            OkHttpClient clone = client.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).build();
            WebSocketCall webSocketCall = WebSocketCall.create(clone, r.build());
            final ContainerInputOutputErrorHandle handle = new ContainerInputOutputErrorHandle(in, out, err, inPipe, outPipe, errPipe);
            webSocketCall.enqueue(handle);
            handle.waitUntilReady();
            return handle;
        } catch (Throwable t) {
            throw DockerClientException.launderThrowable(t);
        }
    }

    @Override
    public InputOutputErrorHandle getLogs() {
        return doAttach(true, false);
    }

    @Override
    public InputOutputErrorHandle stream() {
        return doAttach(false,true);
    }

    @Override
    public StreamGetLogsInterface<InputOutputErrorHandle> readingError(PipedInputStream errPipe) {
        return new AttachContainer(client, config, name, in, out, err, inPipe, outPipe, errPipe);
    }

    @Override
    public StreamGetLogsInterface<InputOutputErrorHandle> writingError(OutputStream err) {
        return new AttachContainer(client, config, name, in, out, err, inPipe, outPipe, errPipe);
    }

    @Override
    public StreamGetLogsInterface<InputOutputErrorHandle> redirectingError() {
        return readingError(new PipedInputStream());
    }

    @Override
    public ContainerOutputErrorStreamGetLogsInterface<InputOutputErrorHandle> readingInput(InputStream in) {
        return new AttachContainer(client, config, name, in, out, err, inPipe, outPipe, errPipe);
    }

    @Override
    public ContainerOutputErrorStreamGetLogsInterface<InputOutputErrorHandle> writingInput(PipedOutputStream inPipe) {
        return new AttachContainer(client, config, name, in, out, err, inPipe, outPipe, errPipe);
    }

    @Override
    public ContainerOutputErrorStreamGetLogsInterface<InputOutputErrorHandle> redirectingInput() {
        return writingInput(new PipedOutputStream());
    }

    @Override
    public ContainerErrorStreamGetLogsInterface<InputOutputErrorHandle> readingOutput(PipedInputStream outPipe) {
        return new AttachContainer(client, config, name, in, out, err, inPipe, outPipe, errPipe);
    }

    @Override
    public ContainerErrorStreamGetLogsInterface<InputOutputErrorHandle> writingOutput(OutputStream out) {
        return new AttachContainer(client, config, name, in, out, err, inPipe, outPipe, errPipe);
    }

    @Override
    public ContainerErrorStreamGetLogsInterface<InputOutputErrorHandle> redirectingOutput() {
        return readingOutput(new PipedInputStream());
    }
}
