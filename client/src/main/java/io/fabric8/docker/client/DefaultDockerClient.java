package io.fabric8.docker.client;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.Info;
import io.fabric8.docker.api.model.Version;
import io.fabric8.docker.client.dsl.DockerDSL;
import io.fabric8.docker.client.dsl.container.ContainerInterface;
import io.fabric8.docker.client.dsl.image.ImageSearchInterface;
import io.fabric8.docker.client.dsl.image.ImagesInterface;
import io.fabric8.docker.client.impl.ContainerOperationImpl;
import io.fabric8.docker.client.impl.ImageOperationImpl;
import io.fabric8.docker.client.utils.HttpClientUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class DefaultDockerClient implements DockerClient {

    private final OkHttpClient client;
    private final URL masterUrl;
    private final Config configuration;

    public DefaultDockerClient() {
        this(new ConfigBuilder().build());
    }

    public DefaultDockerClient(Config configuration) {
        this.configuration = configuration;
        try {
            this.masterUrl = new URL(configuration.getMasterUrl());
            this.client = HttpClientUtils.createHttpClient(configuration);
        } catch (MalformedURLException e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    public Info info() {
        return null;
    }

    public Version version() {
        return null;
    }

    @Override
    public ContainerInterface container() {
        return new ContainerOperationImpl(client, configuration);
    }

    @Override
    public ImagesInterface images() {
        return new ImageOperationImpl(client, configuration);
    }

}
