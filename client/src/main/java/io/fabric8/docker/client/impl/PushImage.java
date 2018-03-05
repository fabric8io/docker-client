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
import io.fabric8.docker.dsl.image.ForceToRegistryInterface;
import io.fabric8.docker.dsl.image.RedirectingWritingOutputTagForceToRegistryInterface;
import io.fabric8.docker.dsl.image.TagForceToRegistryInterface;
import io.fabric8.docker.dsl.image.ToRegistryInterface;
import io.fabric8.docker.dsl.image.UsingListenerRedirectingWritingOutputTagForceToRegistryInterface;

import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.DatatypeConverter;

public class PushImage extends BaseImageOperation implements
        UsingListenerRedirectingWritingOutputTagForceToRegistryInterface<OutputHandle>,
        RedirectingWritingOutputTagForceToRegistryInterface<OutputHandle>,
        TagForceToRegistryInterface<OutputHandle>,
        ForceToRegistryInterface<OutputHandle>,
        ToRegistryInterface<OutputHandle> {

    private static final String PUSH_OPERATION = "push";
    private static final String TAG = "tag";
    private static final String FORCE = "force";

    private final String tag;
    private final Boolean force;
    private final OutputStream out;
    private final EventListener listener;

    public PushImage(OkHttpClient client, Config config, String name) {
        this(client, config, name, null, false, null, NULL_LISTENER);
    }

    public PushImage(OkHttpClient client, Config config, String name, String tag, Boolean force, OutputStream out, EventListener listener) {
        super(client, config, name, PUSH_OPERATION);
        this.tag = tag;
        this.force = force;
        this.out = out;
        this.listener = listener;
    }

    @Override
    public OutputHandle toRegistry() {
        try {
            StringBuilder sb = new StringBuilder().append(getOperationUrl(PUSH_OPERATION));
            sb.append(Q).append(FORCE).append(EQUALS).append(force);
            if (Utils.isNotNullOrEmpty(tag)) {
                sb.append(A).append(TAG).append(EQUALS).append(tag);
            }
            AuthConfig authConfig = RegistryUtils.getConfigForImage(name, config);
            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, "{}");
            Request request = new Request.Builder()
                    .header("X-Registry-Auth", DatatypeConverter.printBase64Binary(JSON_MAPPER.writeValueAsString(authConfig != null ? authConfig : new AuthConfig()).getBytes("UTF-8")))
                    .post(body)
                    .url(sb.toString()).build();

            OkHttpClient clone = client.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).build();
            PushImageHandle handle = new PushImageHandle(out, config.getImagePushTimeout(), TimeUnit.MILLISECONDS, listener);
            clone.newCall(request).enqueue(handle);
            return handle;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public ForceToRegistryInterface<OutputHandle> withTag(String tag) {
        return new PushImage(client, config, name, tag, force, out, listener);
    }

    @Override
    public RedirectingWritingOutputTagForceToRegistryInterface<OutputHandle> usingListener(EventListener listener) {
        return new PushImage(client, config, name, tag, force, out, listener);
    }

    @Override
    public TagForceToRegistryInterface<OutputHandle> redirectingOutput() {
        return new PushImage(client, config, name, tag, force, new PipedOutputStream(), listener);
    }

    @Override
    public TagForceToRegistryInterface<OutputHandle> writingOutput(OutputStream out) {
        return new PushImage(client, config, name, tag, force, out, listener);
    }

    @Override
    public ToRegistryInterface<OutputHandle> force() {
        return force(true);
    }

    @Override
    public ToRegistryInterface<OutputHandle> force(Boolean force) {
        return new PushImage(client, config, name, tag, force, out, listener);
    }
}
