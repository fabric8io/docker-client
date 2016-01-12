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
import io.fabric8.docker.api.model.ContainerInfo;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.URLUtils;
import io.fabric8.docker.dsl.container.ContainerExecResource;
import io.fabric8.docker.dsl.container.ExecInterface;

import java.net.URL;

public class ExecNamedOperationImpl extends OperationSupport implements ContainerExecResource<Boolean, ContainerInfo> {

    public ExecNamedOperationImpl(OkHttpClient client, Config config, String name) {
        super(client, config, EXEC_OPERATION, name, null);
    }

    @Override
    public Boolean resize(int h, int w) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            sb.append("?h=").append(h);
            sb.append("&w=").append(w);
            RequestBody body = RequestBody.create(MEDIA_TYPE_TEXT, "");
            Request.Builder requestBuilder = new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), "resize"));
            handleResponse(requestBuilder, 200);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean start() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getResourceUrl());
            RequestBody body = RequestBody.create(MEDIA_TYPE_TEXT, "");
            Request.Builder requestBuilder = new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), "start"));
            handleResponse(requestBuilder, 204);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public ContainerInfo inspect() {
        return inspect(false);
    }

    @Override
    public ContainerInfo inspect(Boolean withSize) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(getResourceUrl());
            sb.append("?size=" + withSize);
            return handleGet(new URL(sb.toString()), ContainerInfo.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }
}
