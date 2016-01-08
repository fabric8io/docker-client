package io.fabric8.docker.client.dsl;

import io.fabric8.docker.client.dsl.container.ContainerOperation;
import io.fabric8.docker.client.dsl.image.ImageOperation;
import io.fabric8.docker.client.dsl.network.NetworkOperation;
import io.fabric8.docker.client.dsl.volume.VolumeOperation;

public interface DockerDSL
    extends ContainerOperation,
            ImageOperation,
            VolumeOperation,
            NetworkOperation {


}
