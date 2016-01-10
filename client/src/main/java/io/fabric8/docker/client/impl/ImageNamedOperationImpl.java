package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.api.model.ImageHistory;
import io.fabric8.docker.api.model.ImageInspect;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.image.ForceOrAndPruneOrNoPruneInterface;
import io.fabric8.docker.dsl.image.ImageInspectOrHistoryOrPushOrTagOrDeleteInterface;
import io.fabric8.docker.dsl.image.InRepositoryOrForceOrTagNameInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrTagOrToRegistryInterface;

import java.util.List;

public class ImageNamedOperationImpl extends OperationSupport implements ImageInspectOrHistoryOrPushOrTagOrDeleteInterface<ImageInspect, List<ImageHistory>, OutputHandle, Boolean, ImageDelete> {

    private static final String HISTORY_OPERATION = "history";
    private static final String INSPECT_OPERATION = "inspect";

    public ImageNamedOperationImpl(OkHttpClient client, Config config, String name) {
        super(client, config, IMAGES_RESOURCE, name, null);
    }

    @Override
    public ForceOrAndPruneOrNoPruneInterface<ImageDelete> delete() {
        return new io.fabric8.docker.client.impl.ImageDelete(client, config, name);
    }

    @Override
    public List<ImageHistory> history() {
        try {
            return handleList(getOperationUrl(HISTORY_OPERATION), ImageHistory.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public ImageInspect inspect() {
        try {
            return handleGet(getOperationUrl(INSPECT_OPERATION), ImageInspect.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }



    @Override
    public InRepositoryOrForceOrTagNameInterface<Boolean> tag() {
        return new ImageTag(client, config, name);
    }

    @Override
    public UsingListenerOrTagOrToRegistryInterface<OutputHandle> push() {
        return new ImagePush(client, config, name);
    }
}
