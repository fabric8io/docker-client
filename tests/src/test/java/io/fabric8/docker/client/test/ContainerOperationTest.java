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

import io.fabric8.docker.api.model.Container;
import io.fabric8.docker.api.model.ContainerBuilder;
import io.fabric8.docker.api.model.ContainerInfo;
import io.fabric8.docker.api.model.ContainerInfoBuilder;
import io.fabric8.docker.api.model.Image;
import io.fabric8.docker.api.model.ImageBuilder;
import io.fabric8.docker.api.model.ImageInspect;
import io.fabric8.docker.api.model.ImageInspectBuilder;
import io.fabric8.docker.api.model.SearchResult;
import io.fabric8.docker.api.model.SearchResultBuilder;
import io.fabric8.docker.client.DockerClient;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ContainerOperationTest extends DockerMockServerTestBase {

    @Test
    public void testContainerList() {
        List<Container> expectedAll = new LinkedList<>();
        List<Container> expectedRunning = new LinkedList<>();

        expectedAll.add(new ContainerBuilder().withId("cnt1").build());
        expectedAll.add(new ContainerBuilder().withId("cnt2").build());
        expectedAll.add(new ContainerBuilder().withId("cnt3").build());

        expectedRunning.add(new ContainerBuilder().withId("cnt3").build());

        expect().withPath("/containers/json?all=true")
                .andReturn(200, expectedAll)
                .always();

        expect().withPath("/containers/json?all=false")
                .andReturn(200, expectedRunning)
                .always();

        DockerClient client = getClient();

        List<Container> actualAll = client.container().list().all();
        assertEquals(expectedAll, actualAll);

        List<Container> actualEnd = client.container().list().running();
        assertEquals(expectedRunning, actualEnd);
    }



    @Test
    public void testInspectContainer() {
        expect().withPath("/containers/mycnt/json")
                .andReturn(200, new ContainerInfoBuilder().withId("testid").build())
                .once();

        DockerClient client = getClient();

        ContainerInfo inspect = client.container().withName("mycnt").inspect();
        assertNotNull(inspect);
        assertEquals("testid", inspect.getId());
    }



    @Test
    public void testStartContainer() {
        expect().post().withPath("/containers/mycnt/start")
                .andReturn(204, "")
                .once();

        DockerClient client = getClient();

        assertTrue(client.container().withName("mycnt").start());
    }

    @Test
    public void testRestartContainer() {
        expect().post().withPath("/containers/mycnt/restart")
                .andReturn(204, "")
                .once();

        DockerClient client = getClient();

        assertTrue(client.container().withName("mycnt").restart());
    }

    @Test
    public void testStopContainer() {
        expect().post().withPath("/containers/mycnt/stop")
                .andReturn(204, "")
                .once();

        DockerClient client = getClient();

        assertTrue(client.container().withName("mycnt").stop());
    }


    @Test
    public void testKillContainer() {
        expect().post().withPath("/containers/mycnt/kill")
                .andReturn(204, "")
                .once();

        DockerClient client = getClient();

        assertTrue(client.container().withName("mycnt").kill());
    }


    @Test
    @Ignore
    public void testRenameContainer() {
        expect().post().withPath("/containers/mycnt/rename?name=newcnt")
                .andReturn(204, "")
                .once();

        DockerClient client = getClient();
        //TODO: Implement this
    }
}
