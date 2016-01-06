package io.fabric8.docker.api.model;

public class InlineContainerCreate extends io.fabric8.docker.api.model.ContainerCreateRequestFluentImpl<InlineContainerCreate> implements Doneable<ContainerCreateResponse>, ContainerCreateRequestFluent<InlineContainerCreate> {

    private final ContainerCreateRequestBuilder builder;

    public InlineContainerCreate(ContainerCreateRequest item) {
        this.builder = new ContainerCreateRequestBuilder(this, item);
    }

    public InlineContainerCreate(ContainerCreateRequestBuilder builder) {
        this.builder = builder;
    }

    public InlineContainerCreate() {
        this.builder = new ContainerCreateRequestBuilder(this);
    }

    public ContainerCreateResponse doCreate(ContainerCreateRequest request) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public ContainerCreateResponse done() {
        return doCreate(builder.build());
    }

}
