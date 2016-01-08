package io.fabric8.docker.client;

public interface EventListener {

    void onSuccess(String message);

    void onError(String message);

    void onEvent(String event);
}
