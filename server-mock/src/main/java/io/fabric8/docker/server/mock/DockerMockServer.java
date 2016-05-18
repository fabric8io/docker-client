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

package io.fabric8.docker.server.mock;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.mockwebserver.DefaultMockServer;

import java.util.Map;
import java.util.Queue;

public class DockerMockServer extends DefaultMockServer {

    public DockerMockServer() {
        this(true);
    }

    public DockerMockServer(boolean useHttps) {
        super(useHttps);
    }

    public DockerMockServer(MockWebServer server, Map<io.fabric8.mockwebserver.ServerRequest, Queue<io.fabric8.mockwebserver.ServerResponse>> responses, boolean useHttps) {
        super(server, responses, useHttps);
    }

    public void init() {
        start();
    }

    public void destroy() {
        shutdown();
    }

    public DockerClient createClient() {
        Config config = new ConfigBuilder()
                .withDockerUrl(url("/"))
                .withTrustCerts(true)
                .build();
        return new DefaultDockerClient(config);
    }
}
