package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import io.fabric8.docker.api.model.ContainerChange;
import io.fabric8.docker.api.model.ContainerInfo;
import io.fabric8.docker.api.model.ContainerProcessList;
import io.fabric8.docker.api.model.Stats;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.InputOutputErrorHandle;
import io.fabric8.docker.client.OutputHandle;
import io.fabric8.docker.client.dsl.container.ContainerInputOrContainerOutputOrContainerErrorOrStreamOrGetLogsInterface;
import io.fabric8.docker.client.dsl.container.ContainerResourceOrLogsOrInspectOrAttachOrArhciveInterface;
import io.fabric8.docker.client.dsl.container.DownloadFromOrUploadToInterface;
import io.fabric8.docker.client.dsl.container.SinceOrFollowOrDisplayOrContainerOutputOrContainerErrorOrTimestampsOrTailingLinesInterface;
import io.fabric8.docker.client.utils.URLUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

public class ContainerNamedOperationImpl extends BaseContainerOperation implements
        ContainerResourceOrLogsOrInspectOrAttachOrArhciveInterface<ContainerProcessList,List<ContainerChange>,InputStream,Stats,Boolean,OutputHandle,ContainerInfo,InputOutputErrorHandle,OutputStream> {

    private static final String REMOVE_VOLUMES = "v";
    private static final String TIMEOUT = "t";
    private static final String SIGNAL = "signal";
    private static final String SIGINIT = "SIGNIT";

    public ContainerNamedOperationImpl(OkHttpClient client, Config config, String name) {
        super(client, config, name, null);
    }

    @Override
    public DownloadFromOrUploadToInterface<InputStream, OutputStream> arhcive() {
        return new ContainerArchieve(client, config, name);
    }

    @Override
    public ContainerProcessList top() {
        return top(null);
    }

    @Override
    public ContainerProcessList top(String args) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(URLUtils.join(getResourceUrl().toString(), "top"));
            if (args != null && !args.isEmpty()) {
                sb.append("?ps_args=").append(args);
            }
            return handleGet(new URL(sb.toString()), ContainerProcessList.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public List<ContainerChange> changes() {
        try {
            return handleList(new URL(URLUtils.join(getResourceUrl().toString(), "changes")), ContainerChange.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public InputStream export() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            RequestBody body = RequestBody.create(MEDIA_TYPE_TEXT, "");
            Request.Builder requestBuilder = new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), "start"));
            return handleResponseStream(requestBuilder, 200);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Stats stats() {
        return stats(false);
    }

    @Override
    public Stats stats(Boolean stream) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(URLUtils.join(getResourceUrl().toString(), "stats"));
            sb.append("?stream=").append(stream);
            return handleGet(new URL(sb.toString()), Stats.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean resize(int h, int w) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            sb.append("?h=").append(h);
            sb.append("&w=").append(w);
            RequestBody body = RequestBody.create(MEDIA_TYPE_TEXT, "");
            Request.Builder requestBuilder = new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), "resize"));
            handleResponse(requestBuilder, 200);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean start() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            RequestBody body = RequestBody.create(MEDIA_TYPE_TEXT, "");
            Request.Builder requestBuilder = new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), "start"));
            handleResponse(requestBuilder, 204);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean stop() {
        return stop(0);
    }

    @Override
    public Boolean stop(int time) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            sb.append("?").append(TIMEOUT).append("=").append(time);
            RequestBody body = RequestBody.create(MEDIA_TYPE_TEXT, "");
            Request.Builder requestBuilder = new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), "stop"));
            handleResponse(requestBuilder, 204);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean restart() {
        return restart(0);
    }

    @Override
    public Boolean restart(int time) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            sb.append("?").append(TIMEOUT).append("=").append(time);
            RequestBody body = RequestBody.create(MEDIA_TYPE_TEXT, "");
            Request.Builder requestBuilder = new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), "restart"));
            handleResponse(requestBuilder, 204);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean kill() {
        return kill(SIGINIT);
    }

    @Override
    public Boolean kill(String signal) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            sb.append("?").append(SIGNAL).append("=").append(signal);
            RequestBody body = RequestBody.create(MEDIA_TYPE_TEXT, "");
            Request.Builder requestBuilder = new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), "kill"));
            handleResponse(requestBuilder, 204);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean remove() {
        return remove(false);
    }

    @Override
    public Boolean remove(Boolean removeVolumes) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            sb.append("?").append(REMOVE_VOLUMES).append(removeVolumes);
            handleDelete(getResourceUrl());
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public ContainerInfo inspect() {
        return new ContainerInspect(client, config, name).inspect();
    }

    @Override
    public ContainerInfo inspect(Boolean withSize) {
        return new ContainerInspect(client, config, name).inspect(withSize);
    }

    @Override
    public SinceOrFollowOrDisplayOrContainerOutputOrContainerErrorOrTimestampsOrTailingLinesInterface<OutputHandle> logs() {
        return new ContainerLog(client, config, name, null, null, null, null, null, 0, false);
    }

    @Override
    public ContainerInputOrContainerOutputOrContainerErrorOrStreamOrGetLogsInterface<InputOutputErrorHandle> attach() {
        return new ContainerAttach(client, config, name, null, null, null, null, null, null);
    }
}
