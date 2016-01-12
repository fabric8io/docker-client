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
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.image.AndPruneOrNoPruneInterface;
import io.fabric8.docker.dsl.image.ForceOrAndPruneOrNoPruneInterface;

import java.net.URL;

public class ImageDelete extends OperationSupport implements
        ForceOrAndPruneOrNoPruneInterface<io.fabric8.docker.api.model.ImageDelete>,
        AndPruneOrNoPruneInterface<io.fabric8.docker.api.model.ImageDelete> {

    private static final String FORCE = "force";
    private static final String NOPRUNE = "noprune";

    private final Boolean force;

    public ImageDelete(OkHttpClient client, Config config, String name) {
        this(client, config, name, false);
    }

    public ImageDelete(OkHttpClient client, Config config, String name, Boolean force) {
        super(client, config, IMAGES_RESOURCE, name, null);
        this.force = force;
    }

    private io.fabric8.docker.api.model.ImageDelete doDelete(Boolean noprune) {
        try {
            return handleDelete(new URL(new StringBuilder().append(getResourceUrl())
                    .append(Q).append(FORCE).append(EQUALS).append(force)
                    .append(A).append(NOPRUNE).append(EQUALS).append(noprune).toString()), io.fabric8.docker.api.model.ImageDelete.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public io.fabric8.docker.api.model.ImageDelete andPrune() {
        return doDelete(true);
    }

    @Override
    public AndPruneOrNoPruneInterface<io.fabric8.docker.api.model.ImageDelete> force() {
        return new ImageDelete(client, config, name, true);
    }

    @Override
    public io.fabric8.docker.api.model.ImageDelete withNoPrune() {
        return doDelete(true);
    }
}
