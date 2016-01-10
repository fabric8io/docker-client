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
        return new InlineNetworkCreate() {
            @Override
            public NetworkCreateResponse doCreate(NetworkCreate request) {
                return create(request);
            }
        };
    }

    @Override
    public NetworkInspectOrNetworkDeleteOrConnectOrDisconnectInterface<NetworkResource, Boolean> withName(String name) {
        return new NetworkNamedOperationImpl(client, config, name);
    }
}
