package io.fabric8.docker.client.dsl.network;


import io.fabric8.docker.api.model.InlineNetworkCreate;
import io.fabric8.docker.api.model.NetworkCreate;
import io.fabric8.docker.api.model.NetworkCreateResponse;
import io.fabric8.docker.api.model.NetworkResource;
import io.fabric8.docker.client.dsl.annotations.CreateOption;
import io.fabric8.docker.client.dsl.annotations.InspectOption;
import io.fabric8.docker.client.dsl.annotations.ListOption;
import io.fabric8.docker.client.dsl.annotations.NamedOption;
import io.fabric8.docker.client.dsl.image.annotations.RemoveOption;
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
