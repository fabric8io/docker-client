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

import io.fabric8.docker.api.model.ContainerInspect;
import io.fabric8.docker.api.model.ExecStartCheck;
import io.fabric8.docker.api.model.ExecStartCheckBuilder;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.URLUtils;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.container.ContainerErrorUsingListenerExecResourceInterface;
import io.fabric8.docker.dsl.container.ContainerExecResource;
import io.fabric8.docker.dsl.container.ContainerExecResourceOutputErrorUsingListenerInterface;
import io.fabric8.docker.dsl.container.UsingListenerContainerExecResourceInterface;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ExecNamedOperationImpl extends OperationSupport implements
    ContainerExecResourceOutputErrorUsingListenerInterface<Boolean, ContainerInspect>,
    UsingListenerContainerExecResourceInterface<Boolean, ContainerInspect>,
    ContainerErrorUsingListenerExecResourceInterface<Boolean, ContainerInspect> {

    protected static final String EXEC_RESOURCE = "exec";

    private static final String START_OPERATION = "start";
    private static final String RESIZE_OPERATION = "resize";
    private static final String SIZE = "size";

    private final OutputStream out;
    private final OutputStream err;

    private final PipedInputStream outPipe;
    private final PipedInputStream errPipe;
    private final EventListener eventListener;

    public ExecNamedOperationImpl(OkHttpClient client, Config config, String name, OutputStream out,
        OutputStream err, PipedInputStream outPipe, PipedInputStream errPipe,
        EventListener eventListener) {
        super(client, config, EXEC_RESOURCE, name, null);
        this.out = out;
        this.err = err;
        this.outPipe = outPipe;
        this.errPipe = errPipe;
        this.eventListener = eventListener;
    }

    @Override
    public Boolean resize(int h, int w) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            sb.append(Q).append("h").append(EQUALS).append(h);
            sb.append(A).append("w").append(EQUALS).append(w);
            RequestBody body = RequestBody.create(MEDIA_TYPE_TEXT, EMPTY);
            Request.Builder requestBuilder =
                new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), RESIZE_OPERATION));
            handleResponse(requestBuilder, 200);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean start() {
        ExecStartCheck config = new ExecStartCheckBuilder().withDetach(true).withTty(false).build();
        return start(config);
    }

    @Override
    public Boolean start(boolean detached) {
        try {
            ExecStartCheck config = new ExecStartCheckBuilder().withDetach(detached).withTty(false).build();
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, JSON_MAPPER.writeValueAsString(config));
            Request request =
                new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), START_OPERATION)).build();
            OkHttpClient clone = client.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).build();

            ContainerLogHandle containerLogHandle = new ContainerLogHandle(out, err, outPipe, errPipe, eventListener);
            clone.newCall(request).enqueue(containerLogHandle);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    public Boolean start(ExecStartCheck config) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, JSON_MAPPER.writeValueAsString(config));
            Request.Builder requestBuilder =
                new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), START_OPERATION));
            handleResponse(requestBuilder, 200);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    //size not used in inspec exec API
    @Override
    public ContainerInspect inspect(Boolean withSize) {
        return inspect();
    }

    @Override
    public ContainerInspect inspect() {
        try {
            return handleGet(new URL(URLUtils.join(getResourceUrl().toString(), JSON_OPERATION)), ContainerInspect.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public UsingListenerContainerExecResourceInterface<Boolean, ContainerInspect> readingError(PipedInputStream errPipe) {
        return new ExecNamedOperationImpl(this.client, this.config, name, out, err, outPipe, errPipe, eventListener);
    }

    @Override
    public UsingListenerContainerExecResourceInterface<Boolean, ContainerInspect> writingError(OutputStream err) {
        return new ExecNamedOperationImpl(this.client, this.config, name, out, err, outPipe, errPipe, eventListener);
    }

    @Override
    public UsingListenerContainerExecResourceInterface<Boolean, ContainerInspect> redirectingError() {
        return readingError(new PipedInputStream());
    }

    @Override
    public ContainerExecResource<Boolean, ContainerInspect> usingListener(EventListener listener) {
        return new ExecNamedOperationImpl(this.client, this.config, name, out, err, outPipe, errPipe, listener);
    }

    @Override
    public ContainerErrorUsingListenerExecResourceInterface<Boolean, ContainerInspect> readingOutput(
        PipedInputStream outPipe) {
        return new ExecNamedOperationImpl(this.client, this.config, name, out, err, outPipe, errPipe, eventListener);
    }

    @Override
    public ContainerErrorUsingListenerExecResourceInterface<Boolean, ContainerInspect> writingOutput(OutputStream out) {
        return new ExecNamedOperationImpl(this.client, this.config, name, out, err, outPipe, errPipe, eventListener);
    }

    @Override
    public ContainerErrorUsingListenerExecResourceInterface<Boolean, ContainerInspect> redirectingOutput() {
        return readingOutput(new PipedInputStream());
    }
}
