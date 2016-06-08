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

package io.fabric8.docker.dsl.network;


import io.fabric8.docker.api.model.InlineNetworkCreate;
import io.fabric8.docker.api.model.NetworkCreate;
import io.fabric8.docker.api.model.NetworkCreateResponse;
import io.fabric8.docker.api.model.NetworkResource;
import io.fabric8.docker.dsl.annotations.CreateOption;
import io.fabric8.docker.dsl.annotations.InspectOption;
import io.fabric8.docker.dsl.annotations.ListOption;
import io.fabric8.docker.dsl.annotations.NamedOption;
import io.fabric8.docker.dsl.annotations.RemoveOption;
import io.sundr.dsl.annotations.All;
import io.sundr.dsl.annotations.Dsl;
import io.sundr.dsl.annotations.EntryPoint;
import io.sundr.dsl.annotations.InterfaceName;
import io.sundr.dsl.annotations.Multiple;
import io.sundr.dsl.annotations.Terminal;

import java.util.List;

@Dsl
@InterfaceName("NetworkOperation")
public interface NetworkDSL {

    @EntryPoint
    void network();

    @ListOption

    void list();

    @All({ListOption.class})
    @Multiple
    void filters(String key, String value);

    @Terminal
    @All({ListOption.class})
    List<NetworkResource> all();


    @CreateOption
    @Terminal
    @InterfaceName("NetworkCreateInterface")
    NetworkCreateResponse create(NetworkCreate networkCreate);

    @CreateOption
    @Terminal
    @InterfaceName("NetworkCreateInterface")
    InlineNetworkCreate createNew();


    @NamedOption
    void withName(String name);

    @Terminal
    @InspectOption
    @InterfaceName("NetworkInspectInterface")
    NetworkResource inspect();


    @Terminal
    @RemoveOption
    @InterfaceName("NetworkDeleteInterface")
    Boolean delete();

    @Terminal
    @InspectOption
    Boolean connect(String containerId);

    @Terminal
    @InspectOption
    Boolean disconnect(String containerId);

}
