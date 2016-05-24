package io.fabric8.docker.client;
/*
 * 
 * Copyright 2016 Roland Huss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import io.fabric8.docker.api.model.ContainerCreateRequestBuilder;
import io.fabric8.docker.api.model.ContainerCreateResponse;
import io.fabric8.docker.api.model.EditableContainerCreateRequest;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;


public class Issue51Test {

    private static final String CONTAINER_NAME = "my_container";
    private static final String IMAGE_NAME = "mongo:3.2.4";
    private Logger logger = LoggerFactory.getLogger(Issue51Test.class);
    private DockerClient client;

    private String url = "unix:///var/run/docker.sock";
    //private String url = "tcp://192.168.99.100:2376";
    public void createClient() {
        Config config = new ConfigBuilder()
                .withDockerUrl(url)
                .build();

        client = new DefaultDockerClient(config);
    }

    public void closeClient() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void fast() {
        createClient();

        ContainerCreateRequestBuilder builder = new ContainerCreateRequestBuilder();
        EditableContainerCreateRequest ls = builder.withImage(IMAGE_NAME).withName(CONTAINER_NAME).build();
        ContainerCreateResponse containerCreateResponse = client.container().create(ls);
        logger.info(containerCreateResponse.toString());

        closeClient();
        createClient();

        boolean started = client.container().withName(CONTAINER_NAME).start();
        logger.info("Container started: {}",started);
        assertThat(started).isEqualTo(true);

        closeClient();
        createClient();

        boolean stopped = client.container().withName(CONTAINER_NAME).stop();
        logger.info("Container {} stopped: {}",CONTAINER_NAME, stopped);
        assertThat(stopped).isEqualTo(true);

        closeClient();
        createClient();

        boolean removed = client.container().withName(CONTAINER_NAME).remove();
        logger.info("Container {} removed: {}",CONTAINER_NAME,removed);
        assertThat(removed).isEqualTo(true);

        closeClient();
    }

    @Ignore
    @Test
    public void slow() {
        createClient();

        ContainerCreateRequestBuilder builder = new ContainerCreateRequestBuilder();
        EditableContainerCreateRequest ls = builder.withImage(IMAGE_NAME).withName(CONTAINER_NAME).build();
        ContainerCreateResponse containerCreateResponse = client.container().create(ls);
        logger.info(containerCreateResponse.toString());

        boolean started = client.container().withName(CONTAINER_NAME).start();
        logger.info("Container started: {}",started);
        assertThat(started).isEqualTo(true);

        boolean stopped = client.container().withName(CONTAINER_NAME).stop();
        logger.info("Container {} stopped: {}",CONTAINER_NAME, stopped);
        assertThat(stopped).isEqualTo(true);

        boolean removed = client.container().withName(CONTAINER_NAME).remove();
        logger.info("Container {} removed: {}",CONTAINER_NAME,removed);
        assertThat(removed).isEqualTo(true);

        closeClient();
    }

}

