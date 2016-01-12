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

package io.fabric8.docker.client.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.docker.api.model.Info;
import io.fabric8.docker.api.model.InfoBuilder;
import io.fabric8.docker.api.model.Version;
import io.fabric8.docker.api.model.VersionBuilder;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.client.ProgressEventBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

public class MiscOperationsTest extends DockerMockServerTestBase {

    @Test
    public void testVersion() {
        expect().withPath("/version")
                .andReturn(200, new VersionBuilder()
                        .withVersion("testversion")
                        .build())
                .once();

        DockerClient client = getClient();

        Version version = client.version();
        assertNotNull(version);
        assertEquals("testversion", version.getVersion());
    }


    @Test
    public void testInfo() {
        expect().withPath("/info")
                .andReturn(200, new InfoBuilder()
                        .withName("testname")
                        .build())
                .once();

        DockerClient client = getClient();

        Info info = client.info();
        assertNotNull(info);
        assertEquals("testname", info.getName());
    }

    @Test
    public void testPing() {
        expect().withPath("/_ping")
                .andReturn(200, "")
                .once();

        DockerClient client = getClient();

        assertTrue(client.ping());
    }

    @Test
    public void testEvents() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        expect().withPath("/events")
                .andReturn(200, mapper.writeValueAsString(new ProgressEventBuilder().build()))
                .once();

        DockerClient client = getClient();

        assertNotNull(client.events().list());
    }


    @Test
    public void testAuth() throws JsonProcessingException {
        expect().post().withPath("/auth")
                .andReturn(200, "")
                .once();

        DockerClient client = getClient();
        assertTrue(client.auth().withUsername("admin").withPassword("admin").withUsername("admin@somewhere.org").done());
    }
}
