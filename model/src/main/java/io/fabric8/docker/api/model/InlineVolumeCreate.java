package io.fabric8.docker.api.model;

public class InlineVolumeCreate extends VolumeCreateRequestFluentImpl<InlineVolumeCreate> implements Doneable<Volume>, VolumeCreateRequestFluent<InlineVolumeCreate> {

    private final VolumeCreateRequestBuilder builder;

    public InlineVolumeCreate(VolumeCreateRequest item) {
        this.builder = new VolumeCreateRequestBuilder(this, item);
    }

    public InlineVolumeCreate(VolumeCreateRequestBuilder builder) {
        this.builder = builder;
    }

    public InlineVolumeCreate() {
        this.builder = new VolumeCreateRequestBuilder(this);
    }

    public Volume doCreate(VolumeCreateRequest request) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public Volume done() {
        return doCreate(builder.build());
    }

}
