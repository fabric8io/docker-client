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

package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.Callback;
import io.fabric8.docker.api.model.Container;
import io.fabric8.docker.api.model.ContainerChange;
import io.fabric8.docker.api.model.ContainerCreateRequest;
import io.fabric8.docker.api.model.ContainerCreateResponse;
import io.fabric8.docker.api.model.ContainerExecCreateResponse;
import io.fabric8.docker.api.model.ContainerInspect;
import io.fabric8.docker.api.model.ContainerProcessList;
import io.fabric8.docker.api.model.InlineContainerCreate;
import io.fabric8.docker.api.model.InlineExecConfig;
import io.fabric8.docker.api.model.Stats;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.InputOutputErrorHandle;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.container.ContainerExecOrContainerResourceOrLogsOrContainerExecResourceOrAttachOrArhciveInterface;
import io.fabric8.docker.dsl.container.ContainerInterface;
import io.fabric8.docker.dsl.container.LimitOrSinceOrBeforeOrSizeOrFiltersOrAllOrRunningInterface;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

public class ContainerOperationImpl extends OperationSupport implements ContainerInterface {

    private static final String CONTAINERS = "containers";
    private static final String JSON = "json";

    public ContainerOperationImpl(OkHttpClient client, Config config) {
        super(client, config, CONTAINERS, null, null);
    }


    @Override
    public LimitOrSinceOrBeforeOrSizeOrFiltersOrAllOrRunningInterface<List<Container>> list() {
        return new ContainerList(client, config, null, null, null, new HashMap<String, String[]>(), 0);
    }

    @Override
    public ContainerExecOrContainerResourceOrLogsOrContainerExecResourceOrAttachOrArhciveInterface<ContainerExecCreateResponse, InlineExecConfig, ContainerProcessList, List<ContainerChange>, InputStream, Stats, Boolean, OutputHandle, ContainerInspect, InputOutputErrorHandle, OutputStream> withName(String name) {
        return new ContainerNamedOperationImpl(client, config, name);
    }

    @Override
    public ContainerCreateResponse create(ContainerCreateRequest container) {
        try {
            return handleCreate(container, ContainerCreateResponse.class, "create");
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public InlineContainerCreate createNew() {
        return new InlineContainerCreate(new Callback<ContainerCreateRequest, ContainerCreateResponse>() {
            @Override
            public ContainerCreateResponse call(ContainerCreateRequest input) {
                return create(input);
            }
        });
    }
}
