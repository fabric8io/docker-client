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
import io.fabric8.docker.dsl.container.ContainerExecResourceLogsAttachArchiveInterface;
import io.fabric8.docker.dsl.container.ContainerInterface;
import io.fabric8.docker.dsl.container.LimitSinceBeforeSizeFiltersAllRunningInterface;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import okhttp3.OkHttpClient;

public class ContainerOperationImpl extends BaseContainerOperation implements ContainerInterface {

    private static final String CREATE_OPERATION = "create";

    public ContainerOperationImpl(OkHttpClient client, Config config) {
        super(client, config, null, null);
    }

    @Override
    public LimitSinceBeforeSizeFiltersAllRunningInterface<List<Container>> list() {
        return new ListContainer(client, config, null, null, null, new HashMap<String, String[]>(), 0);
    }

    @Override
    public ContainerExecResourceLogsAttachArchiveInterface<ContainerExecCreateResponse, InlineExecConfig, ContainerProcessList, List<ContainerChange>, InputStream, Stats, Boolean, Integer, OutputHandle, ContainerInspect, InputOutputErrorHandle, OutputStream> withName(
        String name) {
        return new ContainerNamedOperationImpl(client, config, name);
    }

    @Override
    public ContainerCreateResponse create(ContainerCreateRequest container) {
        try {
            String dir = "";
            if (container.getName() != null && !container.getName().isEmpty()) {
                dir = "?name=" + container.getName();
            }
            return handleCreate(container, ContainerCreateResponse.class, CREATE_OPERATION, dir);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public InlineContainerCreate createNew() {
        return new InlineContainerCreate(
            new io.fabric8.docker.api.builder.Function<ContainerCreateRequest, ContainerCreateResponse>() {
                @Override
                public ContainerCreateResponse apply(ContainerCreateRequest input) {
                    return create(input);
                }
            });
    }
}
