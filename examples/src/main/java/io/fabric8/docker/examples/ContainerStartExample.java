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

import io.fabric8.docker.api.model.ContainerCreateResponse;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ContainerStartExample {

    public static void main(String args[]) throws InterruptedException, IOException {

        if (args.length < 2) {
            System.err.println("Usage: ContainerInspectExample <docker url>");
            System.err.println("Optionally: ContainerStartExample <docker url> <image>");
            return;
        }

        String dockerUrl = args[0];
        String image = args[1];


        Config config = new ConfigBuilder()
                .withDockerUrl(dockerUrl)
                .build();

        try (DockerClient client = new DefaultDockerClient(config)) {

            final CountDownLatch latch = new CountDownLatch(1);

            try (OutputHandle pullHandle = client.image().withName(image).pull().usingListener(new EventListener() {
                @Override
                public void onSuccess(String message) {
                    latch.countDown();
                }

                @Override
                public void onError(String message) {
                    latch.countDown();
                }

                @Override
                public void onError(Throwable t) {
                    latch.countDown();
                }

                @Override
                public void onEvent(String event) {
                    System.out.println(event);
                }
            }).fromRegistry()) {
                if (!latch.await(5, TimeUnit.MINUTES)) {
                    throw new DockerClientException("Failed to pull image [" + image + "]");
                }
            }

            ContainerCreateResponse container = client.container().createNew()
                    .withName("example")
                    .withImage(image)
                    .done();

            try (OutputHandle logHandle = client.container().withName(container.getId()).logs().writingOutput(System.out).writingError(System.err).display()) {

                if (client.container().withName(container.getId()).start()) {
                    System.out.println("Container started!");
                } else {
                    throw new DockerClientException("Failed to start container.");
                }

                if (client.container().withName(container.getId()).stop()) {
                    System.out.println("Container stopped!");
                } else {
                    throw new DockerClientException("Failed to stop container.");
                }
            }

            client.container().withName(container.getId()).remove();
        }
    }
}
