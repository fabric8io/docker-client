/*
 * Copyright (C) 2016 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.fabric8.docker.dsl;

import io.fabric8.docker.dsl.container.ContainerOperation;
import io.fabric8.docker.dsl.image.ImageOperation;
import io.fabric8.docker.dsl.misc.MiscOperation;
import io.fabric8.docker.dsl.network.NetworkOperation;
import io.fabric8.docker.dsl.volume.VolumeOperation;

public interface DockerDSL
        extends ContainerOperation,
        ImageOperation,
        VolumeOperation,
        NetworkOperation,
        MiscOperation {


}
