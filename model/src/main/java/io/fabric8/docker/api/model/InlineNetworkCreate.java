package io.fabric8.docker.api.model;

public class InlineNetworkCreate extends NetworkCreateFluentImpl<InlineNetworkCreate> implements Doneable<NetworkCreateResponse>, NetworkCreateFluent<InlineNetworkCreate> {

    private final NetworkCreateBuilder builder;

    public InlineNetworkCreate(NetworkCreate item) {
        this.builder = new NetworkCreateBuilder(this, item);
    }

    public InlineNetworkCreate(NetworkCreateBuilder builder) {
        this.builder = builder;
    }

    public InlineNetworkCreate() {
        this.builder = new NetworkCreateBuilder(this);
    }

    public NetworkCreateResponse doCreate(NetworkCreate request) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public NetworkCreateResponse done() {
        return doCreate(builder.build());
    }

}
