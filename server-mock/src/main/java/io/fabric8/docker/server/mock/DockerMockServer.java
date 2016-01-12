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

import com.google.mockwebserver.MockWebServer;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class DockerMockServer {

  private final boolean useHttps;

  private MockWebServer server = new MockWebServer();
  private Map<ServerRequest, Queue<ServerResponse>> responses = new HashMap<>();

  public DockerMockServer() {
    this(true);
  }

  public DockerMockServer(boolean useHttps) {
    this.useHttps = useHttps;
  }

  public void init()  {
    try {
      if (useHttps) {
        server.useHttps(MockSSLContextFactory.create().getSocketFactory(), false);
      }
      server.setDispatcher(new MockDispatcher(responses));
      server.play();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }


  public DockerClient createClient() {
    Config config = new ConfigBuilder()
            .withMasterUrl(server.getUrl("/").toString())
            .withTrustCerts(true)
            .build();
    return new DefaultDockerClient(config);
  }

  public void destroy() throws IOException {
    server.shutdown();
  }

  public MockWebServer getServer() {
    return server;
  }

  public MockServerExpectation expect() {
    return new MockServerExpectationImpl(responses);
  }
}
