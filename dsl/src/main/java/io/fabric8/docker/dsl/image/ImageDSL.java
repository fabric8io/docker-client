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

package io.fabric8.docker.dsl.image;


import io.fabric8.docker.api.model.Image;
import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.api.model.ImageHistory;
import io.fabric8.docker.api.model.ImageInspect;
import io.fabric8.docker.api.model.SearchResult;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.annotations.ImportOption;
import io.fabric8.docker.dsl.annotations.InspectOption;
import io.fabric8.docker.dsl.annotations.ListOption;
import io.fabric8.docker.dsl.annotations.NamedOption;
import io.fabric8.docker.dsl.image.annotations.PullOption;
import io.fabric8.docker.dsl.image.annotations.BuildOption;
import io.fabric8.docker.dsl.image.annotations.HistoryOption;
import io.fabric8.docker.dsl.image.annotations.PushOption;
import io.fabric8.docker.dsl.image.annotations.RemoveOption;
import io.fabric8.docker.dsl.image.annotations.TagOption;
import io.sundr.dsl.annotations.All;
import io.sundr.dsl.annotations.Any;
import io.sundr.dsl.annotations.Dsl;
import io.sundr.dsl.annotations.EntryPoint;
import io.sundr.dsl.annotations.InterfaceName;
import io.sundr.dsl.annotations.Multiple;
import io.sundr.dsl.annotations.Only;
import io.sundr.dsl.annotations.Terminal;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Dsl
@InterfaceName("ImageOperation")
public interface ImageDSL {

    @EntryPoint
    void image();

    @NamedOption
    void withName(String name);

    @Terminal
    @InspectOption
    @InterfaceName("ImageInspectInterface")
    ImageInspect inspect();

    @ListOption
    void list();

    @All({ListOption.class})
    void filter(String key);

    @All({ListOption.class})
    @Multiple
    void filters(String key, String value);

    @Terminal
    @All({ListOption.class})
    List<Image> allImages();

    @Terminal
    @All({ListOption.class})
    List<Image> endImages();

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
    @InterfaceName("RemoveIntermediateInterface")
    void removingIntermediateOnSuccess();

    @All({BuildOption.class})
    @InterfaceName("RemoveIntermediateInterface")
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

    @All({BuildOption.class})
    void usingDockerFile(String dockerFile);

    @Any({BuildOption.class, PushOption.class, PullOption.class, ImportOption.class})
    void usingListener(EventListener listener);

    @Any({BuildOption.class, PushOption.class, PullOption.class, ImportOption.class})
    @InterfaceName("RedirectingWritingOutput")
    void redirectingOutput();

    @Any({BuildOption.class, PushOption.class, PullOption.class, ImportOption.class})
    @InterfaceName("RedirectingWritingOutput")
    void writingOutput(OutputStream out);

    @Terminal
    @All({BuildOption.class})
    @InterfaceName("FromPathInterface")
    OutputHandle fromFolder(String folder);

    @Terminal
    @All({BuildOption.class})
    @InterfaceName("FromPathInterface")
    OutputHandle fromTar(String archive);

    @Terminal
    @All({BuildOption.class})
    @InterfaceName("FromPathInterface")
    OutputHandle fromTar(InputStream is);

    @PullOption
    void pull();

    @Any({PullOption.class, ImportOption.class, PushOption.class})
    void withTag(String tag);

    @Terminal
    @All({PullOption.class})
    OutputHandle fromRegistry();

    @ImportOption
    void importFrom(String source);

    @Terminal
    @All({ImportOption.class})
    OutputHandle asRepo(String src);

    @HistoryOption
    @Terminal
    List<ImageHistory> history();

    @PushOption
    void push();

    @Terminal
    @All({PushOption.class})
    OutputHandle toRegistry();

    @TagOption
    void tag();

    @All({TagOption.class})
    void inRepository(String repository);

    @Any({TagOption.class, RemoveOption.class, PushOption.class})
    void force();

    @Any({TagOption.class, RemoveOption.class, PushOption.class})
    void force(Boolean force);

    @Terminal
    @All({TagOption.class})
    Boolean withTagName(String tagName);

    @RemoveOption
    void delete();

    @Terminal
    @All({RemoveOption.class})
    ImageDelete andPrune();

    @Terminal
    @All({RemoveOption.class})
    ImageDelete andPrune(Boolean prune);

    @Terminal
    @All({RemoveOption.class})
    ImageDelete withNoPrune();

    @Only({})
    @Terminal
    @InterfaceName("ImageSearchInterface")
    List<SearchResult> search(String term);


    @Only(value = {NamedOption.class}, orNone = true)
    @Terminal
    InputStream get();

    @Only(value = {NamedOption.class}, orNone = true)
    @Terminal
    Boolean load(InputStream inputStream);
}
