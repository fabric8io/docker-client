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

public class ImageBuildExample {

    private static final String DEFAULT_IMAGE = "image2";
    private static final String DEFAULT_IMAGE_PATH = ImageBuildExample.class.getClassLoader().getResource(DEFAULT_IMAGE).getFile();

    public static void main(String args[]) throws InterruptedException, IOException {

        if (args.length == 0) {
            System.err.println("Usage: ImageBuildExample <docker host>");
            System.err.println("Optionally: ImageBuildExample <docker host> <repo name> <path>");
            return;
        }

        String dokcerHost = args[0];
        String image = args.length >= 2 ? args[1] : DEFAULT_IMAGE;
        String imageFolder = args.length >= 3 ? args[2] : DEFAULT_IMAGE_PATH;

        Config config = new ConfigBuilder()
                .withMasterUrl(dokcerHost)
                .build();

        DockerClient client = new DefaultDockerClient(config);
        final CountDownLatch buildDone = new CountDownLatch(1);
        final CountDownLatch pushDone = new CountDownLatch(1);


        OutputHandle handle = client.image().build()
                .withRepositoryName(image)
                .usingListener(new EventListener() {
                    @Override
                    public void onSuccess(String message) {
                        System.out.println("Success:" + message);
                        buildDone.countDown();
                    }

                    @Override
                    public void onError(String messsage) {
                        System.err.println("Failure:" +messsage);
                        buildDone.countDown();
                    }

                    @Override
                    public void onEvent(String event) {
                        System.out.println(event);
                    }
                })
                .fromFolder(imageFolder);

        buildDone.await();
        handle.close();

        client.image().withName(image).tag().inRepository(image).force().withTagName("v1");

        handle = client.image().withName(image).push().usingListener(new EventListener() {
            @Override
            public void onSuccess(String message) {
                System.out.println("Success:" + message);
                pushDone.countDown();
            }

            @Override
            public void onError(String message) {
                System.out.println("Error:" + message);
                pushDone.countDown();
            }

            @Override
            public void onEvent(String event) {
                System.out.println(event);

            }
        }).toRegistry();

        pushDone.await();
        handle.close();
        client.close();
    }
}
