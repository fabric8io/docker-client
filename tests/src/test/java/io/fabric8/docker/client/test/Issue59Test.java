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

import io.fabric8.docker.api.model.ContainerInspect;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.client.utils.Utils;
import io.fabric8.docker.server.mock.DockerMockServerTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class Issue59Test extends DockerMockServerTestBase {

    @Test
    public void testContainerInspect() throws IOException {
        String response = Utils.readFully(getClass().getClassLoader().getResourceAsStream("issue59.json"));
        expect().get().withPath("/containers/mycontainer/json").andReturn(200, response).always();

        DockerClient client = getClient();
        ContainerInspect inspect = client.container().withName("mycontainer").inspect();
        Assert.assertNotNull(inspect);
    }
}

