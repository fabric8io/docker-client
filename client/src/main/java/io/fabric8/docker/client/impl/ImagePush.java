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
import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.RegistryUtils;
import io.fabric8.docker.client.utils.Utils;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.image.RedirectingWritingOutputOrTagOrToRegistryOrForceInterface;
import io.fabric8.docker.dsl.image.TagOrToRegistryOrForceInterface;
import io.fabric8.docker.dsl.image.ToRegistryInterface;
import io.fabric8.docker.dsl.image.ToRegistryOrForceInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrRedirectingWritingOutputOrTagOrToRegistryOrForceInterface;
import org.apache.commons.codec.binary.Base64;

import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;

public class ImagePush extends OperationSupport implements
        UsingListenerOrRedirectingWritingOutputOrTagOrToRegistryOrForceInterface<OutputHandle>,
        RedirectingWritingOutputOrTagOrToRegistryOrForceInterface<OutputHandle>,
        TagOrToRegistryOrForceInterface<OutputHandle>,
        ToRegistryOrForceInterface<OutputHandle> {

    private static final String TAG = "tag";
    private static final String FORCE = "force";

    private final String tag;
    private final Boolean force;
    private final OutputStream out;
    private final EventListener listener;

    public ImagePush(OkHttpClient client, Config config, String name) {
        this(client, config, name, null, false, null, NULL_LISTENER);
    }

    public ImagePush(OkHttpClient client, Config config, String name, String tag, Boolean force, OutputStream out, EventListener listener) {
        super(client, config, IMAGES_RESOURCE, name, PUSH_OPERATION);
        this.tag = tag;
        this.force = force;
        this.out = out;
        this.listener = listener;
    }

    @Override
    public OutputHandle toRegistry() {
        try {
            StringBuilder sb = new StringBuilder().append(getOperationUrl(PUSH_OPERATION));
            if (Utils.isNotNullOrEmpty(tag)) {
                sb.append(Q).append(TAG).append(EQUALS).append(tag);
            }
            if (force) {
                sb.append(A).append(TAG).append(EQUALS).append(force);
            }
            AuthConfig authConfig = RegistryUtils.getConfigForImage(name, config);
            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, "{}");
            Request request = new Request.Builder()
                    .header("X-Registry-Auth", new String(Base64.encodeBase64(JSON_MAPPER.writeValueAsString(authConfig != null ? authConfig : new AuthConfig()).getBytes("UTF-8")), "UTF-8"))
                    .post(body)
                    .url(sb.toString()).build();

            ImagePushHandle handle = new ImagePushHandle(out, config.getImagePushTimeout(), TimeUnit.MILLISECONDS, listener);
            client.newCall(request).enqueue(handle);
            return handle;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public ToRegistryOrForceInterface<OutputHandle> withTag(String tag) {
        return new ImagePush(client, config, name, tag, force, out, listener);
    }

    @Override
    public RedirectingWritingOutputOrTagOrToRegistryOrForceInterface<OutputHandle> usingListener(EventListener listener) {
        return new ImagePush(client, config, name, tag, force, out, listener);
    }

    @Override
    public TagOrToRegistryOrForceInterface<OutputHandle> redirectingOutput() {
        return new ImagePush(client, config, name, tag, force, new PipedOutputStream(), listener);
    }

    @Override
    public TagOrToRegistryOrForceInterface<OutputHandle> writingOutput(OutputStream out) {
        return new ImagePush(client, config, name, tag, force, out, listener);
    }

    @Override
    public ToRegistryInterface<OutputHandle> force() {
        return force(true);
    }

    @Override
    public ToRegistryInterface<OutputHandle> force(Boolean force) {
        return new ImagePush(client, config, name, tag, force, out, listener);
    }
}
