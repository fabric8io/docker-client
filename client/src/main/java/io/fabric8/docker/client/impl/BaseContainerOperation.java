package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.client.Config;

public class BaseContainerOperation extends OperationSupport {

    protected static final String CONTAINERS = "containers";
    protected static final String JSON = "json";

    public BaseContainerOperation(OkHttpClient client, Config config, String name, String operationType) {
        super(client, config, CONTAINERS, name, operationType);
    }
}
