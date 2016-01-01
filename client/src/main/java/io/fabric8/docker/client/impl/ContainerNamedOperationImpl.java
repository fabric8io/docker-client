package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.ContainerChange;
import io.fabric8.docker.api.model.ContainerInfo;
import io.fabric8.docker.api.model.ContainerProcessList;
import io.fabric8.docker.api.model.ContainerState;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.InputOutputHandle;
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
        ContainerResourceOrLogsOrInspectOrAttachOrArhciveInterface<ContainerProcessList, List<ContainerChange>, InputStream, ContainerState, Boolean, OutputHandle, ContainerInfo, InputOutputHandle, OutputStream> {

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
        return null;
    }

    @Override
    public ContainerProcessList stats() {
        return null;
    }

    @Override
    public ContainerState stats(Boolean args) {
        return null;
    }

    @Override
    public Boolean resize(int h, int w) {
        return null;
    }

    @Override
    public Boolean start() {
        return null;
    }

    @Override
    public Boolean stop() {
        return null;
    }

    @Override
    public Boolean stop(int time) {
        return null;
    }

    @Override
    public Boolean restart() {
        return null;
    }

    @Override
    public Boolean restart(int time) {
        return null;
    }

    @Override
    public Boolean kill() {
        return null;
    }

    @Override
    public Boolean kill(int signal) {
        return null;
    }

    @Override
    public Boolean remove() {
        return null;
    }

    @Override
    public Boolean remove(Boolean removeVolumes) {
        return null;
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
    public ContainerInputOrContainerOutputOrContainerErrorOrStreamOrGetLogsInterface<InputOutputHandle> attach() {
        return new ContainerAttach(client, config, name, null, null, null, null, null, null);
    }
}
