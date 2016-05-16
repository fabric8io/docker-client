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

package io.fabric8.docker.client;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.api.model.Info;
import io.fabric8.docker.api.model.InlineAuth;
import io.fabric8.docker.api.model.Version;
import io.fabric8.docker.client.impl.ContainerOperationImpl;
import io.fabric8.docker.client.impl.EventOperationImpl;
import io.fabric8.docker.client.impl.ExecOperationImpl;
import io.fabric8.docker.client.impl.ImageOperationImpl;
import io.fabric8.docker.client.impl.NetworkOperationImpl;
import io.fabric8.docker.client.impl.OperationSupport;
import io.fabric8.docker.client.impl.VolumeOperationImpl;
import io.fabric8.docker.client.utils.HttpClientUtils;
import io.fabric8.docker.dsl.container.ContainerInterface;
import io.fabric8.docker.dsl.container.ExecInterface;
import io.fabric8.docker.dsl.container.annotations.ExecOption;
import io.fabric8.docker.dsl.image.ImageInterface;
import io.fabric8.docker.dsl.misc.EventsInterface;
import io.fabric8.docker.dsl.network.NetworkInterface;
import io.fabric8.docker.dsl.volume.VolumeInterface;

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
            this.client = HttpClientUtils.createHttpClient(configuration);
            this.masterUrl = new URL(configuration.getDockerUrl());
        } catch (MalformedURLException e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public InlineAuth auth() {
        return new InlineAuth(new io.fabric8.docker.api.builder.Function<AuthConfig, Boolean>() {
            public Boolean apply(AuthConfig authConfig) {
                try {
                    new OperationSupport(client, configuration, "auth", null, null).handleCreate(authConfig);
                    return true;
                } catch (Exception e) {
                    throw DockerClientException.launderThrowable(e);
                }
            }
        });
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
    public EventsInterface events() {
        return new EventOperationImpl(client, configuration);
    }

    @Override
    public ContainerInterface container() {
        return new ContainerOperationImpl(client, configuration);
    }

    @Override
    public ExecInterface exec() {
        return new ExecOperationImpl(client,configuration, "exec");//TODO: check this
    }

    @Override
    public ImageInterface image() {
        return new ImageOperationImpl(client, configuration);
    }

    @Override
    public NetworkInterface network() {
        return new NetworkOperationImpl(client, configuration);
    }

    @Override
    public VolumeInterface volume() {
        return new VolumeOperationImpl(client, configuration);
    }

    @Override
    public void close() {
        if (client.getConnectionPool() != null) {
            client.getConnectionPool().evictAll();
        }
        client.getDispatcher().getExecutorService().shutdown();
    }
}
