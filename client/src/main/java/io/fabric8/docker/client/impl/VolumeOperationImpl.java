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
import io.fabric8.docker.api.model.InlineVolumeCreate;
import io.fabric8.docker.api.model.Volume;
import io.fabric8.docker.api.model.VolumeCreateRequest;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.volume.FiltersOrAllInterface;
import io.fabric8.docker.dsl.volume.VolumeInspectOrDeleteInterface;
import io.fabric8.docker.dsl.volume.VolumeInterface;

import java.util.List;

public class VolumeOperationImpl extends OperationSupport implements VolumeInterface {


    public VolumeOperationImpl(OkHttpClient client, Config config) {
        super(client, config, VOLUME_RESOURCE, null, null);
    }

    @Override
    public FiltersOrAllInterface<List<Volume>> list() {
        return new VolumeList(client, config);
    }


    @Override
    public VolumeInspectOrDeleteInterface<Volume, Boolean> withName(String name) {
        return new VolumeNamedOperationImpl(client, config, name);
    }

    @Override
    public Volume create(VolumeCreateRequest container) {
        try {
            return handleCreate(container, Volume.class, "create");
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public InlineVolumeCreate createNew() {
        return new InlineVolumeCreate(new io.fabric8.docker.api.builder.Function<VolumeCreateRequest, Volume>() {
            @Override
            public Volume apply(VolumeCreateRequest input) {
                return create(input);
            }
        });
    }
}
