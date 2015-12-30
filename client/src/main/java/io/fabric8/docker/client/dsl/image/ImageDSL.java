package io.fabric8.docker.client.dsl.image;


import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.api.model.ImageHistory;
import io.fabric8.docker.client.dsl.annotations.CreateOption;
import io.fabric8.docker.client.dsl.annotations.InspectOption;
import io.fabric8.docker.client.dsl.annotations.ListOption;
import io.fabric8.docker.client.dsl.annotations.NamedOption;
import io.fabric8.docker.client.dsl.image.annotations.BuildOption;
import io.fabric8.docker.client.dsl.image.annotations.HistoryOption;
import io.fabric8.docker.client.dsl.image.annotations.PushOption;
import io.fabric8.docker.client.dsl.image.annotations.RemoveOption;
import io.fabric8.docker.client.dsl.image.annotations.TagOption;
import io.sundr.dsl.annotations.All;
import io.sundr.dsl.annotations.Any;
import io.sundr.dsl.annotations.Dsl;
import io.sundr.dsl.annotations.EntryPoint;
import io.sundr.dsl.annotations.InterfaceName;
import io.sundr.dsl.annotations.Multiple;
import io.sundr.dsl.annotations.Terminal;

import java.util.List;

@Dsl
@InterfaceName("ImageOperation")
public interface ImageDSL {

    @EntryPoint
    void images();

    @NamedOption
    void withName(String name);

    @Terminal
    @InspectOption
    @InterfaceName("ImageInspectInterface")
    void inspect();

    @ListOption
    void list();

    @ListOption
    @All({ListOption.class})
    @Multiple
    void filters(String key, String value);

    @ListOption
    @All({ListOption.class})
    void filter(String key);

    @Terminal
    @All({ListOption.class})
    void allImages();

    @Terminal
    @All({ListOption.class})
    void imagesWithoutIntermediate();

    @BuildOption
    void build();

    @All({BuildOption.class})
    void withRepositoryName(String t);

    @All({BuildOption.class})
    void supressingVerboseOutput();

    @All({BuildOption.class})
    void withNoCache();

    @All({BuildOption.class})
    void pulling();

    @All({BuildOption.class})
    void removingIntermediateOnSuccess();

    @All({BuildOption.class})
    void alwaysRemovingIntermediate();

    @All({BuildOption.class})
    void withMemory(String size);

    @All({BuildOption.class})
    void withSwap(String size);

    @All({BuildOption.class})
    void withCpuShares(int cpuShares);

    @All({BuildOption.class})
    void withCpus(int cpus);

    @All({BuildOption.class})
    void withCpuPeriod(int microseconds);

    @All({BuildOption.class})
    void withCpuQuota(int microseconds);

    @All({BuildOption.class})
    void withBuildArgs(String buildArgs);

    @Terminal
    @All({BuildOption.class})
    String fromDockerFile(String path);

    @Terminal
    @All({BuildOption.class})
    String fromRemote(String path);

    @Terminal
    @All({BuildOption.class})
    String fromRemote();

    @CreateOption
    void create();

    @All({CreateOption.class})
    void withRepo(String repo);

    @All({CreateOption.class})
    void withTag(String tag);

    @Terminal
    @All({CreateOption.class})
    String fromImage(String image);

    @Terminal
    @All({CreateOption.class})
    String fromSource(String src);


    @Terminal
    @All({CreateOption.class})
    String fromSource();


    @HistoryOption
    @Terminal
    List<ImageHistory> history();

    @PushOption
    @Terminal
    String push();

    @PushOption
    @Terminal
    String push(String tag);

    @TagOption
    void tag();

    @All({TagOption.class})
    void inRepository(String repository);

    @Any({TagOption.class, RemoveOption.class})
    void force();

    @Terminal
    @All({TagOption.class})
    void withTagName(String tagName);

    @RemoveOption
    void delete();

    @Terminal
    @All({RemoveOption.class})
    ImageDelete andPrune();

    @Terminal
    @All({RemoveOption.class})
    ImageDelete withNoPrune();

}
