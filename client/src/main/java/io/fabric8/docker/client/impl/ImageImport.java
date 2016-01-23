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
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.image.AsRepoInterface;
import io.fabric8.docker.dsl.image.RedirectingWritingOutputOrTagOrAsRepoInterface;
import io.fabric8.docker.dsl.image.TagOrAsRepoInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrRedirectingWritingOutputOrTagOrAsRepoInterface;
import io.fabric8.docker.client.utils.RegistryUtils;
import io.fabric8.docker.client.utils.Utils;

import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;

public class ImageImport extends OperationSupport implements
        UsingListenerOrRedirectingWritingOutputOrTagOrAsRepoInterface<OutputHandle>,
        RedirectingWritingOutputOrTagOrAsRepoInterface<OutputHandle>,
        AsRepoInterface<OutputHandle>,
        TagOrAsRepoInterface<OutputHandle> {

    private static final String CREATE_OPERATION = "create";
    private static final String TAG = "tag";

    private final String tag;
    private final String source;
    private final OutputStream out;
    private final EventListener listener;

    public ImageImport(OkHttpClient client, Config config, String source) {
        this(client, config, source, null, null, NULL_LISTENER);
    }

    public ImageImport(OkHttpClient client, Config config, String source, String tag, OutputStream out, EventListener listener) {
        super(client, config, IMAGES_RESOURCE, null, CREATE_OPERATION);
        this.tag = tag;
        this.source = source;
        this.out = out;
        this.listener = listener;
    }

    @Override
    public OutputHandle asRepo(String src) {
        try {
            StringBuilder sb = new StringBuilder().append(getOperationUrl(CREATE_OPERATION));
            if (Utils.isNotNullOrEmpty(tag)) {
                sb.append(Q).append(TAG).append(EQUALS).append(tag);
            }
            AuthConfig authConfig = RegistryUtils.getConfigForImage(name, config);
            Request request = new Request.Builder()
                    .header("X-Registry-Auth", new String(org.apache.commons.codec.binary.Base64.encodeBase64URLSafe(JSON_MAPPER.writeValueAsString(config.getAuthConfigs()).getBytes("UTF-8")), "UTF-8"))
                    .post(RequestBody.create(MEDIA_TYPE_TEXT, EMPTY))
                    .url(sb.toString()).build();

            ImageImportHandle handle = new ImageImportHandle(out, config.getImagePushTimeout(), TimeUnit.MILLISECONDS, listener);
            client.newCall(request).enqueue(handle);
            return handle;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }


    @Override
    public TagOrAsRepoInterface<OutputHandle> redirectingOutput() {
        return new ImageImport(client, config, source, tag, new PipedOutputStream(), listener);
    }

    @Override
    public TagOrAsRepoInterface<OutputHandle> writingOutput(OutputStream out) {
        return new ImageImport(client, config, source, tag, out, listener);
    }

    @Override
    public RedirectingWritingOutputOrTagOrAsRepoInterface<OutputHandle> usingListener(EventListener listener) {
        return new ImageImport(client, config, source, tag, out, listener);
    }

    @Override
    public AsRepoInterface<OutputHandle> withTag(String tag) {
        return new ImageImport(client, config, source, tag, out, listener);
    }
}
