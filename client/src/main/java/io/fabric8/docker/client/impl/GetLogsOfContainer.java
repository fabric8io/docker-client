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

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ws.WebSocketCall;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.URLUtils;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.container.ContainerErrorTimestampsTailingLinesFollowDisplayInterface;
import io.fabric8.docker.dsl.container.ContainerOutputErrorTimestampsTailingLinesFollowDisplayInterface;
import io.fabric8.docker.dsl.container.FollowDisplayInterface;
import io.fabric8.docker.dsl.container.SinceContainerOutputErrorTimestampsTailingLinesFollowDisplayInterface;
import io.fabric8.docker.dsl.container.TailingLinesFollowDisplayInterface;
import io.fabric8.docker.dsl.container.TimestampsTailingLinesFollowDisplayInterface;

import java.io.OutputStream;
import java.io.PipedInputStream;
import java.util.concurrent.TimeUnit;

import static io.fabric8.docker.client.utils.Utils.isNotNullOrEmpty;

public class GetLogsOfContainer extends BaseContainerOperation implements
        SinceContainerOutputErrorTimestampsTailingLinesFollowDisplayInterface<OutputHandle>,
        TimestampsTailingLinesFollowDisplayInterface<OutputHandle>,
        ContainerErrorTimestampsTailingLinesFollowDisplayInterface<OutputHandle>,
        FollowDisplayInterface<OutputHandle>,
        TailingLinesFollowDisplayInterface<OutputHandle> {

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


    public GetLogsOfContainer(OkHttpClient client, Config config, String name, OutputStream out, OutputStream err, PipedInputStream outPipe, PipedInputStream errPipe, String since, int lines, Boolean timestampsEnabled) {
        super(client, config, name, LOG);
        this.out = out;
        this.err = err;
        this.outPipe = outPipe;
        this.errPipe = errPipe;
        this.since = since;
        this.lines = lines;
        this.timestampsEnabled = timestampsEnabled;
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

            if(timestampsEnabled) {
                sb.append("&").append(TIMESTAMPS).append("=").append(TRUE);
            }

            Request request = new Request.Builder().url(sb.toString()).get().build();
            OkHttpClient clone = client.clone();
            clone.setReadTimeout(0, TimeUnit.MILLISECONDS);

            final ContainerLogHandle handle = new ContainerLogHandle(out, err, outPipe, errPipe);
            clone.newCall(request).enqueue(handle);
            handle.waitUntilReady();
            return handle;
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
    public TimestampsTailingLinesFollowDisplayInterface<OutputHandle> readingError(PipedInputStream errPipe) {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, timestampsEnabled);
    }

    @Override
    public TimestampsTailingLinesFollowDisplayInterface<OutputHandle> writingError(OutputStream err) {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, timestampsEnabled);
    }

    @Override
    public TimestampsTailingLinesFollowDisplayInterface<OutputHandle> redirectingError() {
        return readingError(new PipedInputStream());
    }


    @Override
    public ContainerErrorTimestampsTailingLinesFollowDisplayInterface<OutputHandle> readingOutput(PipedInputStream outPipe) {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, timestampsEnabled);
    }

    @Override
    public ContainerErrorTimestampsTailingLinesFollowDisplayInterface<OutputHandle> writingOutput(OutputStream out) {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, timestampsEnabled);
    }

    @Override
    public ContainerErrorTimestampsTailingLinesFollowDisplayInterface<OutputHandle> redirectingOutput() {
        return readingOutput(new PipedInputStream());
    }

    @Override
    public ContainerOutputErrorTimestampsTailingLinesFollowDisplayInterface<OutputHandle> since(String since) {
        return null;
    }

    @Override
    public FollowDisplayInterface<OutputHandle> tailingLines(int lines) {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, timestampsEnabled);
    }

    @Override
    public TailingLinesFollowDisplayInterface<OutputHandle> withTimestamps() {
        return new GetLogsOfContainer(client, config, name, out, err, outPipe, errPipe, since, lines, true);
    }
}
