package io.fabric8.docker.client.dsl.container;

import io.fabric8.docker.api.model.Container;
import io.fabric8.docker.api.model.ContainerChange;
import io.fabric8.docker.api.model.ContainerCreateResponse;
import io.fabric8.docker.api.model.ContainerInfo;
import io.fabric8.docker.api.model.ContainerProcessList;
import io.fabric8.docker.api.model.ContainerState;
import io.fabric8.docker.client.dsl.annotations.CreateOption;
import io.fabric8.docker.client.dsl.annotations.InspectOption;
import io.fabric8.docker.client.dsl.annotations.ListOption;
import io.fabric8.docker.client.dsl.annotations.NamedOption;
import io.fabric8.docker.client.dsl.annotations.OtherOption;
import io.fabric8.docker.client.dsl.container.annotations.ArchiveOption;
import io.fabric8.docker.client.dsl.container.annotations.AttachOption;
import io.fabric8.docker.client.dsl.container.annotations.LogOption;
import io.sundr.dsl.annotations.All;
import io.sundr.dsl.annotations.Any;
import io.sundr.dsl.annotations.Dsl;
import io.sundr.dsl.annotations.EntryPoint;
import io.sundr.dsl.annotations.InterfaceName;
import io.sundr.dsl.annotations.Multiple;
import io.sundr.dsl.annotations.None;
import io.sundr.dsl.annotations.Terminal;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.List;

@Dsl
@InterfaceName("ContainerOperation")
public interface ContainerDSL {

    @EntryPoint
    void container();

    @NamedOption
    void withName(String name);

    @Terminal
    @CreateOption
    ContainerCreateResponse create(Object container);

    @ListOption
    @None({NamedOption.class})
    void list();

    @All({ListOption.class})
    void limit(int number);

    @Any({ListOption.class, LogOption.class})
    void since(String id);

    @All({ListOption.class})
    void before(String id);

    @All({ListOption.class})
    void size(String id);

    @All({ListOption.class})
    @Multiple
    void filters(String key, String value);

    @Terminal
    @All({ListOption.class})
    List<Container> all();

    @Terminal
    @All({ListOption.class})
    List<Container> running();

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    ContainerProcessList top();

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    ContainerProcessList top(String args);

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    List<ContainerChange> changes();

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    InputStream export();

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    ContainerProcessList stats();

    @Terminal
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    @OtherOption
    ContainerState stats(Boolean args);

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    Boolean resize(int h,int w);

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    Boolean start();

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    Boolean stop();

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    Boolean stop(int time);

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    Boolean restart();

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    Boolean restart(int time);

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    Boolean kill();

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    Boolean kill(int signal);

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    Boolean remove();

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    Boolean remove(Boolean removeVolumes);


    @LogOption
    @All({NamedOption.class})
    void logs();

    @Terminal
    @All({LogOption.class})
    void follow();

    @Terminal
    @All({LogOption.class})
    void display();

    @Any({LogOption.class, AttachOption.class})
    @InterfaceName("ContainerLogInputInterface")
    void readingInput(InputStream in);

    @Any({LogOption.class, AttachOption.class})
    @InterfaceName("ContainerLogInputInterface")
    void writingInput(PipedOutputStream in);

    @Any({LogOption.class, AttachOption.class})
    @InterfaceName("ContainerLogInputInterface")
    void redirectingInput();

    @Any({LogOption.class, AttachOption.class})
    @InterfaceName("ContainerLogOutputInterface")
    void readingOutput(InputStream in);

    @Any({LogOption.class, AttachOption.class})
    @InterfaceName("ContainerLogOutputInterface")
    void writingOutput(PipedOutputStream in);

    @Any({LogOption.class, AttachOption.class})
    @InterfaceName("ContainerLogOutputInterface")
    void redirectingOutput();

    @Any({LogOption.class, AttachOption.class})
    @InterfaceName("ContainerLogErrorInterface")
    void readingError(InputStream in);

    @Any({LogOption.class, AttachOption.class})
    @InterfaceName("ContainerLogErrorInterface")
    void writingError(PipedOutputStream in);

    @All({LogOption.class})
    @InterfaceName("ContainerLogErrorInterface")
    void redirectingError();

    @All({LogOption.class})
    void withTimestamps();

    @All({LogOption.class})
    void tailingLines(int number);

    @Terminal
    @InspectOption
    ContainerInfo inspect();

    @Terminal
    @InspectOption
    ContainerInfo inspect(Boolean withSize);

    @AttachOption
    @All({NamedOption.class})
    void attach();

    @Terminal
    @All({AttachOption.class})
    void stream();


    @Terminal
    @All({AttachOption.class})
    void getLogs();

    @ArchiveOption
    @All({NamedOption.class})
    void arhcive();

    @Terminal
    @All({ArchiveOption.class})
    InputStream downloadFrom(String path);

    @Terminal
    @All({ArchiveOption.class})
    OutputStream uploadTo(String path);

}
