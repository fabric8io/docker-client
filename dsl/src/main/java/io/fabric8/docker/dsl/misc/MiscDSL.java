package io.fabric8.docker.dsl.misc;

import io.fabric8.docker.api.model.Info;
import io.fabric8.docker.api.model.InlineAuth;
import io.fabric8.docker.api.model.Version;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.annotations.ListOption;
import io.fabric8.docker.dsl.misc.annotations.EventsOption;
import io.sundr.dsl.annotations.All;
import io.sundr.dsl.annotations.Dsl;
import io.sundr.dsl.annotations.EntryPoint;
import io.sundr.dsl.annotations.InterfaceName;
import io.sundr.dsl.annotations.Multiple;
import io.sundr.dsl.annotations.Terminal;

@Dsl
@InterfaceName("MiscOperation")
public interface MiscDSL {

    @EntryPoint
    @Terminal
    Info info();

    @EntryPoint
    @Terminal
    InlineAuth auth();

    @EntryPoint
    @Terminal
    Version version();

    @EntryPoint
    @Terminal
    Boolean ping();

    @EntryPoint
    @EventsOption
    void events();

    @All(EventsOption.class)
    void since(String since);

    @All(EventsOption.class)
    void until(String until);

    @All({EventsOption.class})
    @Multiple
    void filters(String key, String value);

    @Terminal
    @All(EventsOption.class)
    OutputHandle list();
}
