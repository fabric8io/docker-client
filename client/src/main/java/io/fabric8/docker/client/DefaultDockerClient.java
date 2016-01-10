package io.fabric8.docker.client;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.api.model.Info;
import io.fabric8.docker.api.model.InlineAuth;
import io.fabric8.docker.api.model.Version;
import io.fabric8.docker.dsl.container.ContainerInterface;
import io.fabric8.docker.dsl.image.ImageInterface;
import io.fabric8.docker.dsl.network.NetworkInterface;
import io.fabric8.docker.dsl.volume.VolumeInterface;
import io.fabric8.docker.client.impl.ContainerOperationImpl;
import io.fabric8.docker.client.impl.ImageOperationImpl;
import io.fabric8.docker.client.impl.OperationSupport;
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

    @Override
    public InlineAuth auth() {
        return new InlineAuth() {
            @Override
            public Boolean doAuth(AuthConfig authConfig) {
                try {
                     new OperationSupport(client, configuration, "auth", null, null).handleCreate(authConfig);
                     return true;
                } catch (Exception e) {
                    throw DockerClientException.launderThrowable(e);
                }
            }
        };
    }

    public Info info() {
        try {
            return new OperationSupport(client, configuration, "info", null, null).handleGet(Info.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    public Version version() {
        try {
            return new OperationSupport(client, configuration, "version", null, null).handleGet(Version.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean ping() {
        try {
             new OperationSupport(client, configuration, "_ping", null, null).handleGet();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ContainerInterface container() {
        return new ContainerOperationImpl(client, configuration);
    }

    @Override
    public ImageInterface image() {
        return new ImageOperationImpl(client, configuration);
    }

    @Override
    public NetworkInterface network() {
        return null;
    }

    @Override
    public VolumeInterface volume() {
        return null;
    }
}
