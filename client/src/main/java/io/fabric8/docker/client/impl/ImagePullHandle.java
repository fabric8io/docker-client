package io.fabric8.docker.client.impl;

import io.fabric8.docker.client.EventListener;
import io.fabric8.docker.client.ProgressEvent;
import io.fabric8.docker.client.utils.Utils;

import java.util.concurrent.TimeUnit;

public class ImagePullHandle extends EventHandle {

    private static final String STATUS = "Status:";

    public ImagePullHandle(long duration, TimeUnit unit, EventListener listener) {
        super(duration, unit, listener);
    }

    @Override
    public boolean isSuccess(ProgressEvent event) {
        return Utils.isNotNullOrEmpty(event.getStream()) && event.getStream().startsWith(STATUS);
    }

    @Override
    public boolean isFailure(ProgressEvent event) {
        return Utils.isNotNullOrEmpty(event.getError());
    }
}
