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
import io.fabric8.docker.api.model.ContainerExecCreateResponse;
import io.fabric8.docker.api.model.ContainerInspect;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.container.ContainerExecResource;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ContainerExecStartExample {

    public static void main(String args[]) throws InterruptedException, IOException {
        OutputHandle handle = null;
        if (args.length < 2) {
            System.err.println("Usage: ContainerLogsExample <docker url>");
            System.err.println("Optionally: ContainerLogsExample <docker url> <container id>");
            return;
        }

        String dockerUrl = args[0];
        String image = args[1];

        Config config = new ConfigBuilder()
            .withDockerUrl(dockerUrl)
            .build();

        DockerClient client = new DefaultDockerClient(config);
        try {
            ContainerCreateResponse container = client.container().createNew()
                .withName("example")
                .withImage(image)
                .done();

            if (client.container().withName(container.getId()).start()) {
                System.out.println("Container started!");
            } else {
                throw new DockerClientException("Failed to start container.");
            }

            final String[] commands = {"sh", "-c", "ping localhost"};

            final ContainerExecCreateResponse done =
                client.container().withName(container.getId()).execNew().withCmd(commands)
                    .withAttachStderr(true).withAttachStdout(true).withTty(false)
                    .withDetach(false)
                    .done();

            CountDownLatch countDownLatch = new CountDownLatch(1);

            handle = client.exec().withName(done.getId()).
                writingOutput(System.out).writingError(System.err)
                .usingListener(new EventListener() {
                    @Override
                    public void onSuccess(String message) {
                    }

                    @Override
                    public void onError(String message) {
                    }

                    @Override
                    public void onEvent(String event) {
                        System.out.println("*****" + event);
                    }
                }).start(false);
            countDownLatch.await(10, TimeUnit.SECONDS);

            if (client.container().withName(container.getId()).stop()) {
                System.out.println("Container stopped!");
            } else {
                throw new DockerClientException("Failed to stop container.");
            }

            client.container().withName(container.getId()).remove();
        } finally {
            if (handle != null) {
                handle.close();
            }
            System.out.close();
            System.err.close();
            client.close();
        }
    }
}
