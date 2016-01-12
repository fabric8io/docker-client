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
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import io.fabric8.docker.api.model.NetworkResource;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.network.NetworkInspectOrNetworkDeleteOrConnectOrDisconnectInterface;

public class NetworkNamedOperationImpl extends OperationSupport implements NetworkInspectOrNetworkDeleteOrConnectOrDisconnectInterface<NetworkResource, Boolean>{

    private static final String INSPECT_OPERATION = "inspect";
    private static final String CONENCT_OPERATION = "connect";
    private static final String DISCONENCT_OPERATION = "disconnect";

    private static final String CONTAINER = "container";

    public NetworkNamedOperationImpl(OkHttpClient client, Config config, String name) {
        super(client, config, NETWORK_RESOURCE, name, null);
    }

    private Boolean containerOp(String containerId, String opertaionType) {
        try {
            RequestBody body = RequestBody.create(MEDIA_TYPE_TEXT, EMPTY);

            Request.Builder requestBuilder = new Request.Builder().post(body).url(new StringBuilder()
                    .append(getOperationUrl(opertaionType))
                    .append(Q).append(CONTAINER).append(EQUALS).append(containerId).toString());

            handleResponse(requestBuilder, 201);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean connect(String containerId) {
        return containerOp(containerId, CONENCT_OPERATION);
    }

    @Override
    public Boolean disconnect(String containerId) {
        return containerOp(containerId, DISCONENCT_OPERATION);
    }

    @Override
    public Boolean delete() {
        try {
            handleDelete(getOperationUrl());
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public NetworkResource inspect() {
        try {
            return handleGet(getOperationUrl(INSPECT_OPERATION), NetworkResource.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }
}
