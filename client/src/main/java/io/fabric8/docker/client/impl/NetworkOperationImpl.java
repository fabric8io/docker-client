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


import io.fabric8.docker.api.model.InlineNetworkCreate;
import io.fabric8.docker.api.model.NetworkCreate;
import io.fabric8.docker.api.model.NetworkCreateResponse;
import io.fabric8.docker.api.model.NetworkResource;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.network.FiltersOrAllInterface;
import io.fabric8.docker.dsl.network.NetworkInspectOrNetworkDeleteOrConnectOrDisconnectInterface;
import io.fabric8.docker.dsl.network.NetworkInterface;

import java.util.List;

public class NetworkOperationImpl extends OperationSupport implements NetworkInterface {

    @Override
    public FiltersOrAllInterface<List<NetworkResource>> list() {
        return new NetworkList(client, config);
    }

    @Override
    public NetworkCreateResponse create(NetworkCreate networkCreate) {
        try {
            return handleCreate(networkCreate, NetworkCreateResponse.class, "create");
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public InlineNetworkCreate createNew() {
        return new InlineNetworkCreate(new io.fabric8.docker.api.builder.Function<NetworkCreate, NetworkCreateResponse>() {
            @Override
            public NetworkCreateResponse apply(NetworkCreate input) {
                return create(input);
            }
        });
    }

    @Override
    public NetworkInspectOrNetworkDeleteOrConnectOrDisconnectInterface<NetworkResource, Boolean> withName(String name) {
        return new NetworkNamedOperationImpl(client, config, name);
    }
}
