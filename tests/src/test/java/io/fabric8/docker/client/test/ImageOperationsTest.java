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

import io.fabric8.docker.api.model.Image;
import io.fabric8.docker.api.model.ImageBuilder;
import io.fabric8.docker.api.model.ImageHistory;
import io.fabric8.docker.api.model.ImageHistoryBuilder;
import io.fabric8.docker.api.model.ImageInspect;
import io.fabric8.docker.api.model.ImageInspectBuilder;
import io.fabric8.docker.api.model.SearchResult;
import io.fabric8.docker.api.model.SearchResultBuilder;
import io.fabric8.docker.client.DockerClient;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImageOperationsTest extends DockerMockServerTestBase {

    @Test
    public void testImageList() {
        List<Image> expectedAll = new LinkedList<>();
        List<Image> expectedEnd = new LinkedList<>();

        expectedAll.add(new ImageBuilder().withId("image1").build());
        expectedAll.add(new ImageBuilder().withId("image2").build());
        expectedAll.add(new ImageBuilder().withId("image3").build());

        expectedEnd.add(new ImageBuilder().withId("image3").build());

        expect().withPath("/images/json?all=true")
                .andReturn(200, expectedAll)
                .always();

        expect().withPath("/images/json?all=false")
                .andReturn(200, expectedEnd)
                .always();

        DockerClient client = getClient();

        List<Image> actualAll = client.image().list().allImages();
        assertEquals(expectedAll, actualAll);

        List<Image> actualEnd = client.image().list().endImages();
        assertEquals(expectedEnd, actualEnd);
    }


    @Test
    public void testSearchImages() {
        List<SearchResult> expectedSearchResult = new LinkedList<>();
        expectedSearchResult.add(new SearchResultBuilder().withName("image1").build());
        expectedSearchResult.add(new SearchResultBuilder().withName("image2").build());
        expectedSearchResult.add(new SearchResultBuilder().withName("image3").build());

        expect().withPath("/images/search?term=image")
                .andReturn(200, expectedSearchResult)
                .once();

        expect().withPath("/images/search?term=nonexistent")
                .andReturn(200, Collections.emptyList())
                .once();

        DockerClient client = getClient();

        List<SearchResult> actualResult = client.image().search("image");
        assertEquals(expectedSearchResult, actualResult);

        actualResult = client.image().search("nonexistent");
        assertEquals(Collections.emptyList(), actualResult);
    }

    @Test
    public void testInspectImage() {
        expect().withPath("/images/myimage/json")
                .andReturn(200, new ImageInspectBuilder().withId("testid").build())
                .once();

        DockerClient client = getClient();

        ImageInspect inspect = client.image().withName("myimage").inspect();
        assertNotNull(inspect);
        assertEquals("testid", inspect.getId());
    }

    @Test
    public void testImageHistory() {
        List<ImageHistory> expectedHistory = new LinkedList<>();
        expectedHistory.add(new ImageHistoryBuilder().withId("history1").build());
        expectedHistory.add(new ImageHistoryBuilder().withId("history3").build());
        expectedHistory.add(new ImageHistoryBuilder().withId("history3").build());

        expect().withPath("/images/myimage/history")
                .andReturn(200, expectedHistory)
                .once();

        DockerClient client = getClient();

        List<ImageHistory> history = client.image().withName("myimage").history();
        assertNotNull(history);
        assertEquals(expectedHistory, expectedHistory);
    }

}
