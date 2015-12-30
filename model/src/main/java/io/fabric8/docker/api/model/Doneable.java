package io.fabric8.docker.api.model;

public interface Doneable<T> {
    T done();
}
