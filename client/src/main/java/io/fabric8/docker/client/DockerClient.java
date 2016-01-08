package io.fabric8.docker.client;

import io.fabric8.docker.api.model.Info;
import io.fabric8.docker.api.model.InlineAuth;
import io.fabric8.docker.api.model.Version;
import io.fabric8.docker.client.dsl.DockerDSL;

public interface DockerClient extends DockerDSL {

    InlineAuth auth();

    Info info();

    Version version();

    Boolean ping();
}
