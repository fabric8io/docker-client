package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.ContainerInfo;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.dsl.container.InspectInterface;

import java.net.URL;

public class ContainerInspect extends BaseContainerOperation implements InspectInterface<ContainerInfo> {
    public ContainerInspect(OkHttpClient client, Config config, String name) {
        super(client, config, name, JSON);
    }

    @Override
    public ContainerInfo inspect() {
        return inspect(false);
    }

    @Override
    public ContainerInfo inspect(Boolean withSize) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(getResourceUrl());
            sb.append("?size=" + withSize);
            return handleGet(new URL(sb.toString()), ContainerInfo.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }
}
