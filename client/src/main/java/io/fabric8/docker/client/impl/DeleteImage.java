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

package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.image.AndPruneNoInterface;
import io.fabric8.docker.dsl.image.ForceAndPruneNoInterface;

import java.net.URL;
import java.util.List;

;

public class DeleteImage extends BaseImageOperation implements
        ForceAndPruneNoInterface<List<ImageDelete>>,
        AndPruneNoInterface<List<ImageDelete>> {

    private static final String FORCE = "force";
    private static final String NOPRUNE = "noprune";

    private final Boolean force;

    public DeleteImage(OkHttpClient client, Config config, String name) {
        this(client, config, name, false);
    }

    public DeleteImage(OkHttpClient client, Config config, String name, Boolean force) {
        super(client, config, name, null);
        this.force = force;
    }

    public List<ImageDelete> andPrune(Boolean noprune) {
        try {
            return handleDelete(new URL(new StringBuilder().append(getResourceUrl())
                    .append(Q).append(FORCE).append(EQUALS).append(force)
                    .append(A).append(NOPRUNE).append(EQUALS).append(noprune).toString()),
                    JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, ImageDelete.class));
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public List<ImageDelete> andPrune() {
        return andPrune(true);
    }

    @Override
    public AndPruneNoInterface<List<ImageDelete>> force() {
        return force(true);
    }

    @Override
    public AndPruneNoInterface<List<ImageDelete>> force(Boolean force) {
        return new DeleteImage(client, config, name, force);
    }

    @Override
    public List<ImageDelete> withNoPrune() {
        return andPrune(false);
    }
}
