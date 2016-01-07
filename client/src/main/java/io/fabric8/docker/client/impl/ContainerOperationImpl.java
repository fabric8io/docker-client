package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.Container;
import io.fabric8.docker.api.model.ContainerChange;
import io.fabric8.docker.api.model.ContainerCreateRequest;
import io.fabric8.docker.api.model.ContainerCreateResponse;
import io.fabric8.docker.api.model.ContainerInfo;
import io.fabric8.docker.api.model.ContainerProcessList;
import io.fabric8.docker.api.model.InlineContainerCreate;
import io.fabric8.docker.api.model.Stats;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.InputOutputErrorHandle;
import io.fabric8.docker.client.OutputHandle;
import io.fabric8.docker.client.dsl.container.ContainerInterface;
import io.fabric8.docker.client.dsl.container.ContainerResourceOrLogsOrInspectOrAttachOrArhciveInterface;
import io.fabric8.docker.client.dsl.container.LimitOrSinceOrBeforeOrSizeOrFiltersOrAllOrRunningInterface;
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
    public ContainerResourceOrLogsOrInspectOrAttachOrArhciveInterface<ContainerProcessList, List<ContainerChange>, InputStream, Stats, Boolean, OutputHandle, ContainerInfo, InputOutputErrorHandle, OutputStream> withName(String name) {
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
        return new InlineContainerCreate() {
            @Override
            public ContainerCreateResponse doCreate(ContainerCreateRequest request) {
                return create(request);
            }
        };
    }
}
