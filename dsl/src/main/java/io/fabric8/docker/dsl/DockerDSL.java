package io.fabric8.docker.dsl;

import io.fabric8.docker.dsl.container.ContainerOperation;
import io.fabric8.docker.dsl.image.ImageOperation;
import io.fabric8.docker.dsl.network.NetworkOperation;
import io.fabric8.docker.dsl.volume.VolumeOperation;

public interface DockerDSL
    extends ContainerOperation,
            ImageOperation,
            VolumeOperation,
            NetworkOperation {


}
