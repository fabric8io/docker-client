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
import io.fabric8.docker.api.model.Volume;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.volume.VolumeInspectDeleteInterface;

public class VolumeNamedOperationImpl extends BaseVolumeOperation implements VolumeInspectDeleteInterface<Volume, Boolean> {

    private static final String INSPECT_OPERATION = "inspect";

    public VolumeNamedOperationImpl(OkHttpClient client, Config config, String name) {
        super(client, config, name, null);
    }

    @Override
    public Boolean delete() {
        try {
            handleDelete(getOperationUrl());
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Volume inspect() {
        try {
            return handleGet(getOperationUrl(EMPTY), Volume.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }
}
