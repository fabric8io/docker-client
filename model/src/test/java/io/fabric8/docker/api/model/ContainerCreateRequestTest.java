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

package io.fabric8.docker.api.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ContainerCreateRequestTest {

    private static final int BUFFER_SIZE = 8192;
    private static final String DEFAULT_CONTAINER_NAME = "cnt";
    private static final String DEFAULT_NETWORK_NAME = "net";
    private static final String DEFAULT_IMAGE = "mongo:3.2.4";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testIssue50() throws JsonProcessingException, JSONException {

        ContainerCreateRequest request = new io.fabric8.docker.api.model.ContainerCreateRequestBuilder()
                .withNewHostConfig()
                .addToPortBindings("1337/tcp", new ArrayList<PortBinding>(Arrays.asList(
                        new io.fabric8.docker.api.model.PortBindingBuilder()
                                .withHostIp("0.0.0.0")
                                .withHostPort("1337")
                                .build())))
                .withNetworkMode(DEFAULT_NETWORK_NAME)
                .endHostConfig()
                .withImage(DEFAULT_IMAGE)
                .withName(DEFAULT_CONTAINER_NAME)
                .addToExposedPorts(1337, Protocol.TCP)
                .build();

        String requestJson = OBJECT_MAPPER.writeValueAsString(request);
        System.out.println(requestJson);

        Map<String, ArrayList<PortBinding>> portBindings = request.getHostConfig().getPortBindings();


        String expected = readStream(getClass().getClassLoader().getResourceAsStream("portbinding.json"));
        JSONAssert.assertEquals(expected, OBJECT_MAPPER.writeValueAsString(portBindings), false);
    }



    public static String readStream(InputStream is) {
        final char[] buffer = new char[BUFFER_SIZE];
        final StringBuilder sb = new StringBuilder();
        int r = 0;
        try (Reader in = new InputStreamReader(is, "UTF-8")) {
            while ((r = in.read(buffer, 0, buffer.length)) >= 0) {
                sb.append(buffer, 0, r);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return sb.toString();
    }
}
