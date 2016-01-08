package io.fabric8.docker.client.dsl.volume;

import io.fabric8.docker.api.model.InlineVolumeCreate;
import io.fabric8.docker.api.model.Volume;
import io.fabric8.docker.api.model.VolumeCreateRequest;
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
@InterfaceName("VolumeOperation")
public interface VolumeDSL {

    @EntryPoint
    void volume();

    @ListOption
    void list();

    @All({ListOption.class})
    @Multiple
    void filters(String key, String value);

    @Terminal
    @All({ListOption.class})
    List<Volume> all();

    @CreateOption
    @Terminal
    @InterfaceName("VolumeCreateInterface")
    Volume create(VolumeCreateRequest request);

    @CreateOption
    @Terminal
    @InterfaceName("VolumeCreateInterface")
    InlineVolumeCreate createNew();

    @NamedOption
    void withName(String name);

    @Terminal
    @InspectOption
    @InterfaceName("VolumeInspectInterface")
    Volume inspect();


    @Terminal
    @RemoveOption
    @InterfaceName("VolumeDeleteInterface")
    Boolean delete();

}
