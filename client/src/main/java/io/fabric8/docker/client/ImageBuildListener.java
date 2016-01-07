package io.fabric8.docker.client;

public interface ImageBuildListener {

    void onSuccess(String imagedId);

    void onError(String messsage);

    void onEvent(String event);
}
