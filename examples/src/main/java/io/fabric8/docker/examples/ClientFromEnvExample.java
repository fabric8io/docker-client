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

package io.fabric8.docker.examples;

import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;

import java.io.IOException;

/**
 * Simple example to demonstrate how to autoconfigure the client using env vars.
 * Just set the DOCKER_HOST env var to point to the docker daemon.
 */
public class ClientFromEnvExample {

    public static void main(String args[]) throws InterruptedException, IOException {
        DockerClient client = new DefaultDockerClient();
        System.out.println(client.info());

    }
}
