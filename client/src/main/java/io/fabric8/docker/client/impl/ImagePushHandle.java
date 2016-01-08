package io.fabric8.docker.client.impl;

import io.fabric8.docker.client.EventListener;
import io.fabric8.docker.client.ProgressEvent;
import io.fabric8.docker.client.utils.Utils;

import java.util.concurrent.TimeUnit;

public class ImagePushHandle extends EventHandle {

    private static final String SUCCESSFULLY_BUILT = "Successfully built";

    public ImagePushHandle(long duration, TimeUnit unit, EventListener listener) {
        super(duration, unit, listener);
    }

    @Override
    public boolean isSuccess(ProgressEvent event) {
        return Utils.isNotNullOrEmpty(event.getStream()) && event.getStream().startsWith(SUCCESSFULLY_BUILT);
    }

    @Override
    public boolean isFailure(ProgressEvent event) {
        return Utils.isNotNullOrEmpty(event.getError());
    }
}
