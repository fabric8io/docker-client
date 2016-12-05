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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.RegistryUtils;
import io.fabric8.docker.client.utils.Utils;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.image.FromRegistryInterface;
import io.fabric8.docker.dsl.image.RedirectingWritingOutputTagFromRegistryInterface;
import io.fabric8.docker.dsl.image.TagFromRegistryInterface;
import io.fabric8.docker.dsl.image.UsingListenerRedirectingWritingOutputTagFromRegistryInterface;
import org.apache.commons.codec.binary.Base64;

import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;

public class PullImage extends BaseImageOperation implements
        UsingListenerRedirectingWritingOutputTagFromRegistryInterface<OutputHandle>,
        RedirectingWritingOutputTagFromRegistryInterface<OutputHandle>,
        TagFromRegistryInterface<OutputHandle> {


    private static final String CREATE_OPERATION = "create";
    private static final String FROM_IMAGE = "fromImage";
    private static final String TAG = "tag";

    private final String name;
    private final String tag;
    private final OutputStream out;
    private final EventListener listener;

    public PullImage(OkHttpClient client, Config config, String name) {
        this(client, config, name, null, null, NULL_LISTENER);
    }

    public PullImage(OkHttpClient client, Config config, String name, String tag, OutputStream out, EventListener listener) {
        super(client, config, null, CREATE_OPERATION);
        this.name = name;
        this.tag = tag;
        this.out = out;
        this.listener = listener;
    }

    @Override
    public FromRegistryInterface<OutputHandle> withTag(String tag) {
        return new PullImage(client, config, name, tag, out, listener);
    }

    @Override
    public RedirectingWritingOutputTagFromRegistryInterface<OutputHandle> usingListener(EventListener listener) {
        return new PullImage(client, config, name, tag, out, listener);
    }

    @Override
    public TagFromRegistryInterface<OutputHandle> redirectingOutput() {
        return new PullImage(client, config, name, tag, new PipedOutputStream(), listener);
    }

    @Override
    public TagFromRegistryInterface<OutputHandle> writingOutput(OutputStream out) {
        return new PullImage(client, config, name, tag, out, listener);
    }

    @Override
    public OutputHandle fromRegistry() {
        try {
            StringBuilder sb = new StringBuilder().append(getOperationUrl(CREATE_OPERATION))
                    .append(Q).append(FROM_IMAGE).append(EQUALS).append(name);

            if (Utils.isNotNullOrEmpty(tag)) {
                sb.append(A).append(TAG).append(EQUALS).append(tag);
            }

            AuthConfig authConfig = RegistryUtils.getConfigForImage(name, config);
            Request request = new Request.Builder()
                    .header("X-Registry-Auth", new String(Base64.encodeBase64(JSON_MAPPER.writeValueAsString(authConfig != null ? authConfig : new AuthConfig()).getBytes("UTF-8")), "UTF-8"))
                    .post(RequestBody.create(MEDIA_TYPE_TEXT, EMPTY))
                    .url(sb.toString()).build();

            OkHttpClient clone = client.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).build();
            PullImageHandle handle = new PullImageHandle(out, config.getImagePushTimeout(), TimeUnit.MILLISECONDS, listener);
            clone.newCall(request).enqueue(handle);
            return handle;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }
}
