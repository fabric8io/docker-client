package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.Volume;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.volume.VolumeInspectOrDeleteInterface;

public class VolumeNamedOperationImpl extends OperationSupport implements VolumeInspectOrDeleteInterface<Volume, Boolean> {

    private static final String INSPECT_OPERATION = "inspect";

    public VolumeNamedOperationImpl(OkHttpClient client, Config config, String name) {
        super(client, config, VOLUME_RESOURCE, name, null);
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
            return handleGet(getOperationUrl(INSPECT_OPERATION), Volume.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }
}
