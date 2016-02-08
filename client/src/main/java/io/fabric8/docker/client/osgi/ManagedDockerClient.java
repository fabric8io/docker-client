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

package io.fabric8.docker.client.osgi;

import io.fabric8.docker.api.model.Info;
import io.fabric8.docker.api.model.InlineAuth;
import io.fabric8.docker.api.model.Version;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.client.utils.Utils;
import io.fabric8.docker.dsl.container.ContainerInterface;
import io.fabric8.docker.dsl.container.ExecInterface;
import io.fabric8.docker.dsl.image.ImageInterface;
import io.fabric8.docker.dsl.misc.EventsInterface;
import io.fabric8.docker.dsl.network.NetworkInterface;
import io.fabric8.docker.dsl.volume.VolumeInterface;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import java.io.IOException;
import java.util.Map;

import static io.fabric8.docker.client.Config.*;

@Component(immediate = true, configurationPid = "io.fabric8.docker.client", policy = ConfigurationPolicy.OPTIONAL)
@Service(DockerClient.class)
public class ManagedDockerClient implements DockerClient {

    @Property(name = DOCKER_HOST, description = "Docker Host", value = "http://localhost:2375")
    private String dockerHost = Utils.getSystemPropertyOrEnvVar(Config.DOCKER_HOST, "http://localhost:2375");

    @Property(name = DOCKER_REQUEST_TIMEOUT_SYSTEM_PROPERTY, description = "Request timeout", intValue = 10000)
    private int requestTimeout = Integer.parseInt(Utils.getSystemPropertyOrEnvVar(Config.DOCKER_REQUEST_TIMEOUT_SYSTEM_PROPERTY, "10000"));

    @Property(name = DOCKER_HTTP_PROXY, description = "HTTP Proxy")
    private String httpProxy = Utils.getSystemPropertyOrEnvVar(Config.DOCKER_HTTP_PROXY);
    @Property(name = DOCKER_HTTPS_PROXY, description = "HTTPS Proxy")
    private String httpsProxy = Utils.getSystemPropertyOrEnvVar(Config.DOCKER_HTTPS_PROXY);
    @Property(name = DOCKER_ALL_PROXY, description = "All Proxy")
    private String allProxy = Utils.getSystemPropertyOrEnvVar(Config.DOCKER_ALL_PROXY);
    @Property(name = DOCKER_NO_PROXY, description = "No Proxy")
    private String noProxy = Utils.getSystemPropertyOrEnvVar(Config.DOCKER_NO_PROXY);


    private DockerClient delegate;

    @Activate
    public void activate(Map<String, Object> properties) {
        String noProxyProperty = (String) properties.get(DOCKER_NO_PROXY);
        String[] noProxy = noProxyProperty != null ? noProxyProperty.split(",") : null;

        Config config = new ConfigBuilder()
                .withDockerUrl(dockerHost)
                .withRequestTimeout(requestTimeout)
                .withHttpProxy((String) properties.get(DOCKER_HTTP_PROXY))
                .withHttpsProxy((String) properties.get(DOCKER_HTTPS_PROXY))
                .withNoProxy(noProxy)
                .build();
        delegate = new DefaultDockerClient(config);
    }

    @Deactivate
    public void deactivate() throws IOException {
        delegate.close();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public ContainerInterface container() {
        return delegate.container();
    }

    @Override
    public ExecInterface exec() {
        return delegate.exec();
    }

    @Override
    public ImageInterface image() {
        return delegate.image();
    }

    @Override
    public Info info() {
        return delegate.info();
    }

    @Override
    public InlineAuth auth() {
        return delegate.auth();
    }

    @Override
    public Version version() {
        return delegate.version();
    }

    @Override
    public Boolean ping() {
        return delegate.ping();
    }

    @Override
    public EventsInterface events() {
        return delegate.events();
    }

    @Override
    public NetworkInterface network() {
        return delegate.network();
    }

    @Override
    public VolumeInterface volume() {
        return delegate.volume();
    }
}
