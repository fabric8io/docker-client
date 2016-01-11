package io.fabric8.docker.api.model;

public class InlineExecConfig extends io.fabric8.docker.api.model.ExecConfigFluentImpl<InlineExecConfig> implements Doneable<ContainerExecCreateResponse> {

    private final io.fabric8.docker.api.model.ExecConfigBuilder builder;
    private final Callback<ExecConfig, ContainerExecCreateResponse> callback;

    public InlineExecConfig(Callback<ExecConfig, ContainerExecCreateResponse> callback) {
        this.callback = callback;
        this.builder = new io.fabric8.docker.api.model.ExecConfigBuilder(this);
    }

    @Override
    public ContainerExecCreateResponse done() {
        return callback.call(builder.build());
    }
}
