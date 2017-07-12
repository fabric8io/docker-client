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

package io.fabric8.docker.dsl.container;

import io.fabric8.docker.api.model.Container;
import io.fabric8.docker.api.model.ContainerChange;
import io.fabric8.docker.api.model.ContainerCreateRequest;
import io.fabric8.docker.api.model.ContainerCreateResponse;
import io.fabric8.docker.api.model.ContainerExecCreateResponse;
import io.fabric8.docker.api.model.ContainerInspect;
import io.fabric8.docker.api.model.ContainerProcessList;
import io.fabric8.docker.api.model.ExecConfig;
import io.fabric8.docker.api.model.InlineContainerCreate;
import io.fabric8.docker.api.model.InlineExecConfig;
import io.fabric8.docker.api.model.Stats;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.InputOutputErrorHandle;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.annotations.CreateOption;
import io.fabric8.docker.dsl.annotations.InspectOption;
import io.fabric8.docker.dsl.annotations.ListOption;
import io.fabric8.docker.dsl.annotations.NamedOption;
import io.fabric8.docker.dsl.annotations.OtherOption;
import io.fabric8.docker.dsl.container.annotations.ArchiveOption;
import io.fabric8.docker.dsl.container.annotations.AttachOption;
import io.fabric8.docker.dsl.container.annotations.ExecOption;
import io.fabric8.docker.dsl.container.annotations.LogOption;
import io.fabric8.docker.dsl.container.annotations.UploadOption;
import io.sundr.dsl.annotations.All;
import io.sundr.dsl.annotations.Any;
import io.sundr.dsl.annotations.Dsl;
import io.sundr.dsl.annotations.EntryPoint;
import io.sundr.dsl.annotations.InterfaceName;
import io.sundr.dsl.annotations.Multiple;
import io.sundr.dsl.annotations.Only;
import io.sundr.dsl.annotations.Or;
import io.sundr.dsl.annotations.Terminal;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

@Dsl
@InterfaceName("ContainerOperation")
public interface ContainerDSL {

    @EntryPoint
    void container();

    @EntryPoint
    @ExecOption
    void exec();

    @Only(methods = "create") //Means no option at all
    @Or //We use @Or here to tell in order to call withName, you either need no option or exec options.
    @Any({ExecOption.class})
    @NamedOption
    void withName(String name);

    @CreateOption
    @Terminal
    @InterfaceName("ContainerCreateInterface")
    ContainerCreateResponse create(ContainerCreateRequest container);

    @CreateOption
    @Terminal
    @InterfaceName("ContainerCreateInterface")
    InlineContainerCreate createNew();

    @ListOption
    @Only(methods = "container")
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
    @InterfaceName("ContainerExecInterface")
    ContainerExecCreateResponse exec(ExecConfig execConfig);

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerExecInterface")
    InlineExecConfig execNew();

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
    Stats stats();

    @Terminal
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    @OtherOption
    Stats stats(Boolean args);

    @Terminal
    @All(NamedOption.class)
    @Only({NamedOption.class, ExecOption.class})
    @InterfaceName("ContainerExecResource")
    Boolean resize(int h, int w);

    @Terminal
    @All(NamedOption.class)
    @Only({NamedOption.class, ExecOption.class})
    @InterfaceName("ContainerExecResource")
    Boolean start();

    @Terminal
    @Only({ExecOption.class})
    @InterfaceName("ContainerExecResource")
    OutputHandle start(boolean detached);

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
    Boolean kill(String signal);

    @Terminal
    @OtherOption
    @All({NamedOption.class})
    @InterfaceName("ContainerResource")
    Integer waitContainer();

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
    OutputHandle follow();

    @Terminal
    @All({LogOption.class})
    OutputHandle display();

    @Any({AttachOption.class})
    @InterfaceName("ContainerInputInterface")
    void readingInput(InputStream in);

    @Any({AttachOption.class})
    @InterfaceName("ContainerInputInterface")
    void writingInput(PipedOutputStream in);

    @Any({LogOption.class, AttachOption.class})
    @InterfaceName("ContainerInputInterface")
    void redirectingInput();

    @Any({LogOption.class, AttachOption.class, ExecOption.class})
    @InterfaceName("ContainerOutputInterface")
    void readingOutput(PipedInputStream outPipe);

    @Any({LogOption.class, AttachOption.class, ExecOption.class})
    @InterfaceName("ContainerOutputInterface")
    void writingOutput(OutputStream out);

    @Any({LogOption.class, AttachOption.class, ExecOption.class})
    @InterfaceName("ContainerOutputInterface")
    void redirectingOutput();

    @Any({LogOption.class, AttachOption.class, ExecOption.class})
    @InterfaceName("ContainerErrorInterface")
    void readingError(PipedInputStream errPipe);

    @Any({LogOption.class, AttachOption.class, ExecOption.class})
    @InterfaceName("ContainerErrorInterface")
    void writingError(OutputStream err);

    @All({LogOption.class, ExecOption.class})
    @InterfaceName("ContainerErrorInterface")
    void redirectingError();

    @All({LogOption.class})
    void withTimestamps();

    @All({LogOption.class})
    void tailingLines(int number);

    @Any({LogOption.class, ExecOption.class})
    void usingListener(EventListener listener);

    @Terminal
    @All(NamedOption.class)
    @Any({InspectOption.class, ExecOption.class})
    @InterfaceName("ContainerExecResource")
    ContainerInspect inspect();

    @Terminal
    @All(NamedOption.class)
    @Any({InspectOption.class, ExecOption.class})
    @InterfaceName("ContainerExecResource")
    ContainerInspect inspect(Boolean withSize);

    @AttachOption
    @All({NamedOption.class})
    void attach();

    @Terminal
    @All({AttachOption.class})
    InputOutputErrorHandle stream();

    @Terminal
    @All({AttachOption.class})
    InputOutputErrorHandle getLogs();

    @ArchiveOption
    @All({NamedOption.class})
    void archive();

    @Terminal
    @All({ArchiveOption.class})
    InputStream downloadFrom(String path);

    @UploadOption
    @All({ArchiveOption.class})
    void uploadTo(String path);

    @All({ArchiveOption.class, UploadOption.class})
    void withNoOverwriteDirNonDir(boolean noOverwriteDirNonDir);

    @Terminal
    @Any({ArchiveOption.class, UploadOption.class})
    Boolean withHostResource(String resource);

    @Terminal
    @Any({ArchiveOption.class, UploadOption.class})
    Boolean withTarInputStream(InputStream tarInputStream);
}
