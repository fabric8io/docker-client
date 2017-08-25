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

import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ImagePushExample {

    private static final String DEFAULT_IMAGE = "image1";
    private static final String DEFAULT_IMAGE_PATH = ImagePushExample.class.getClassLoader().getResource(DEFAULT_IMAGE).getFile();

    public static void main(String args[]) throws InterruptedException, IOException {

        if (args.length == 0) {
            System.err.println("Usage: ImagePushExample <docker url>");
            System.err.println("Optionally: ImagePushExample <docker url> <registry> <registry>");
            return;
        }

        String dockerUrl = args[0];
        String image = args.length >= 2 ? args[1] : DEFAULT_IMAGE;
        String registry = args.length >= 3 ? args[2] : "index.docker.io";
        String namespace = args.length >= 4 ? args[3] : "default";

        String repositoryName = registry + "/" + namespace + "/" + image;

        Config config = new ConfigBuilder()
                .withDockerUrl(dockerUrl)
                .build();

        DockerClient client = new DefaultDockerClient(config);
        final CountDownLatch buildDone = new CountDownLatch(1);
        final CountDownLatch pushDone = new CountDownLatch(1);

        client.image().withName(image).tag().inRepository(repositoryName).force().withTagName("1.0");

        OutputHandle handle = client.image().withName(repositoryName).push()
                .usingListener(new EventListener() {
                    @Override
                    public void onSuccess(String message) {
                        System.out.println("Success:" + message);
                        pushDone.countDown();
                    }

                    @Override
                    public void onError(String messsage) {
                        System.err.println("Failure:" +messsage);
                        pushDone.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace(System.err);
                        pushDone.countDown();
                    }

                    @Override
                    public void onEvent(String event) {
                        System.out.println(event);
                    }
                })
                .withTag("1.0")
                .toRegistry();

        pushDone.await();
        handle.close();
        client.close();
    }
}
