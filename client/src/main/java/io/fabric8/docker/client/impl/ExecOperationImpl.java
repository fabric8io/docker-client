package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.ContainerInfo;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.dsl.container.ContainerExecResource;
import io.fabric8.docker.dsl.container.ExecInterface;

public class ExecOperationImpl extends OperationSupport implements ExecInterface {

    public ExecOperationImpl(OkHttpClient client, Config config, String resourceType) {
        super(client, config, resourceType);
    }

    @Override
    public ContainerExecResource<Boolean, ContainerInfo> withName(String name) {
        return null;
    }
}
