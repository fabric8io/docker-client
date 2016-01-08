package io.fabric8.docker.api.model;

public class InlineAuth extends io.fabric8.docker.api.model.AuthConfigFluentImpl<InlineAuth>
    implements Doneable<Boolean> {

    private final io.fabric8.docker.api.model.AuthConfigBuilder builder;

    public InlineAuth() {
        this.builder = new io.fabric8.docker.api.model.AuthConfigBuilder(this);
    }

    public InlineAuth(io.fabric8.docker.api.model.AuthConfigBuilder builder) {
        this.builder = builder;
    }

    @Override
    public Boolean done() {
        return doAuth(builder.build());
    }

    public Boolean doAuth(AuthConfig authConfig) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
