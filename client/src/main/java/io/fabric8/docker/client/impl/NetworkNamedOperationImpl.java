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

import io.fabric8.docker.api.model.NetworkResource;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.network.NetworkInspectDeleteConnectDisconnectInterface;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NetworkNamedOperationImpl extends BaseNetworkOperation implements NetworkInspectDeleteConnectDisconnectInterface<NetworkResource, Boolean> {

    private static final String INSPECT_OPERATION = "inspect";
    private static final String CONNECT_OPERATION = "connect";
    private static final String DISCONNECT_OPERATION = "disconnect";

    private static final String CONTAINER = "container";

    public NetworkNamedOperationImpl(OkHttpClient client, Config config, String name) {
        super(client, config, name, null);
    }

    private Boolean containerOp(String containerId, String opertaionType) {
        try {

            final String jsonString = "{\"" + CONTAINER + "\":\"" + containerId + "\"}";
            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, jsonString);

            Request.Builder requestBuilder = new Request.Builder().post(body).url(new StringBuilder()
                    .append(getOperationUrl(opertaionType)).toString());

            handleResponse(requestBuilder, 200);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean connect(String containerId) {
        return containerOp(containerId, CONNECT_OPERATION);
    }

    @Override
    public Boolean disconnect(String containerId) {
        return containerOp(containerId, DISCONNECT_OPERATION);
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
            return handleGet(getOperationUrl(EMPTY), NetworkResource.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }
}
