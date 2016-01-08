package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.ContainerCreateRequest;
import io.fabric8.docker.api.model.ContainerCreateResponse;
import io.fabric8.docker.api.model.InlineContainerCreate;
import io.fabric8.docker.api.model.InlineVolumeCreate;
import io.fabric8.docker.api.model.Volume;
import io.fabric8.docker.api.model.VolumeCreateRequest;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.dsl.volume.FiltersOrAllInterface;
import io.fabric8.docker.client.dsl.volume.VolumeInspectOrDeleteInterface;
import io.fabric8.docker.client.dsl.volume.VolumeInterface;

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
        return new InlineVolumeCreate() {
            @Override
            public Volume doCreate(VolumeCreateRequest request) {
                return create(request);
            }
        };
    }
}
