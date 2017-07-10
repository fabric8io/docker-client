/*
 * Copyright (C) 2016 iginal Authors
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

import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.URLUtils;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.container.ContainerErrorTimestampsTailingLinesUsingListenerFollowDisplayInterface;
import io.fabric8.docker.dsl.container.ContainerOutputErrorTimestampsTailingLinesUsingListenerFollowDisplayInterface;
import io.fabric8.docker.dsl.container.FollowDisplayInterface;
import io.fabric8.docker.dsl.container.SinceContainerOutputErrorTimestampsTailingLinesUsingListenerFollowDisplayInterface;
import io.fabric8.docker.dsl.container.TailingLinesUsingListenerFollowDisplayInterface;
import io.fabric8.docker.dsl.container.TimestampsTailingLinesUsingListenerFollowDisplayInterface;
import io.fabric8.docker.dsl.container.UsingListenerFollowDisplayInterface;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static io.fabric8.docker.client.utils.Utils.isNotNullOrEmpty;

public class GetLogsOfContainer extends BaseContainerOperation implements
    TailingLinesUsingListenerFollowDisplayInterface<OutputHandle>,
    ContainerOutputErrorTimestampsTailingLinesUsingListenerFollowDisplayInterface<OutputHandle>,
    SinceContainerOutputErrorTimestampsTailingLinesUsingListenerFollowDisplayInterface<OutputHandle>,
    TimestampsTailingLinesUsingListenerFollowDisplayInterface<OutputHandle>,
    ContainerErrorTimestampsTailingLinesUsingListenerFollowDisplayInterface<OutputHandle>,
    UsingListenerFollowDisplayInterface<OutputHandle>,
    FollowDisplayInterface<OutputHandle> {

    private static final String LOG = "logs";
    private static final String FOLLOW = "follow";
    private static final String STDOUT = "stdout";
    private static final String STDERR = "stderr";
    private static final String SINCE = "since";
    private static final String TAIL = "tail";
    private static final String ALL = "all";
    private static final String TIMESTAMPS = "timestamps";

    private final OutputStream out;
    private final OutputStream err;

    private final PipedInputStream outPipe;
    private final PipedInputStream errPipe;

    private final String since;
    private final int lines;
    private final Boolean timestampsEnabled;

    private final EventListener eventListener;

    public GetLogsOfContainer(OkHttpClient client, Config config, String name, OutputStream out, OutputStream err,
        PipedInputStream outPipe, PipedInputStream errPipe, String since, int lines, Boolean timestampsEnabled,
        EventListener eventListener) {
        super(client, config, name, LOG);
        this.out = out;
        this.err = err;
        this.outPipe = outPipe;
        this.errPipe = errPipe;
        this.since = since;
        this.lines = lines;
        this.timestampsEnabled = timestampsEnabled;
        this.eventListener = eventListener;
    }

    public GetLogsOfContainer(OkHttpClient client, Config config, String name, OutputStream out, OutputStream err,
        PipedInputStream outPipe, PipedInputStream errPipe, String since, int lines, Boolean timestampsEnabled) {
        super(client, config, name, LOG);
        this.out = out;
        this.err = err;
        this.outPipe = outPipe;
        this.errPipe = errPipe;
        this.since = since;
        this.lines = lines;
        this.timestampsEnabled = timestampsEnabled;
        this.eventListener = NULL_LISTENER;
    }

    private OutputHandle doGetLogHandle(Boolean follow) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(URLUtils.join(getOperationUrl().toString()));
            sb.append("?").append(FOLLOW).append("=").append(follow ? TRUE : FLASE);
            sb.append("&").append(STDOUT).append("=").append(out != null || outPipe != null ? TRUE : FLASE);
            sb.append("&").append(STDERR).append("=").append(err != null || errPipe != null ? TRUE : FLASE);

            if (isNotNullOrEmpty(since)) {
                sb.append("&").append(SINCE).append("=").append(since);
            }

            if (lines > 0) {
                sb.append("&").append(TAIL).append("=").append(lines);
            } else {
                sb.append("&").append(TAIL).append("=").append(ALL);
            }

            if (timestampsEnabled) {
                sb.append("&").append(TIMESTAMPS).append("=").append(TRUE);
            }

            Request request = new Request.Builder().url(sb.toString()).get().build();
            OkHttpClient clone = client.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).build();

            ContainerLogHandle containerLogHandle = new ContainerLogHandle(out, err, outPipe, errPipe, eventListener);
            clone.newCall(request).enqueue(containerLogHandle);
            return containerLogHandle;
        } catch (Throwable t) {
            throw DockerClientException.launderThrowable(t);
        }
    }

    @Override
    public OutputHandle display() {
        return doGetLogHandle(false);
    }

    @Override
    public OutputHandle follow() {
        return doGetLogHandle(true);
    }

    @Override
    public TimestampsTailingLinesUsingListenerFollowDisplayInterface<OutputHandle> readingError(PipedInputStream errPipe) {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, timestampsEnabled);
    }

    @Override
    public TimestampsTailingLinesUsingListenerFollowDisplayInterface<OutputHandle> writingError(OutputStream err) {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, timestampsEnabled, eventListener);
    }

    @Override
    public TimestampsTailingLinesUsingListenerFollowDisplayInterface<OutputHandle> redirectingError() {
        return readingError(new PipedInputStream());
    }

    @Override
    public ContainerErrorTimestampsTailingLinesUsingListenerFollowDisplayInterface<OutputHandle> readingOutput(
        PipedInputStream outPipe) {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, timestampsEnabled);
    }

    @Override
    public ContainerErrorTimestampsTailingLinesUsingListenerFollowDisplayInterface<OutputHandle> writingOutput(OutputStream out) {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, timestampsEnabled, eventListener);
    }

    @Override
    public ContainerErrorTimestampsTailingLinesUsingListenerFollowDisplayInterface<OutputHandle> redirectingOutput() {
        return readingOutput(new PipedInputStream());
    }

    @Override
    public ContainerOutputErrorTimestampsTailingLinesUsingListenerFollowDisplayInterface<OutputHandle> since(String id) {
        return null;
    }

    @Override
    public UsingListenerFollowDisplayInterface<OutputHandle> tailingLines(int lines) {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, timestampsEnabled, eventListener);
    }

    @Override
    public TailingLinesUsingListenerFollowDisplayInterface<OutputHandle> withTimestamps() {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, true, eventListener);
    }

    @Override
    public FollowDisplayInterface<OutputHandle> usingListener(
        EventListener listener) {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, timestampsEnabled,
            listener);
    }
}
