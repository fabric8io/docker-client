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
import io.fabric8.docker.dsl.image.TagOrAsRepoInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrTagOrAsRepoInterface;
import io.fabric8.docker.client.utils.RegistryUtils;
import io.fabric8.docker.client.utils.Utils;

import java.util.concurrent.TimeUnit;

public class ImageImport extends OperationSupport implements
        UsingListenerOrTagOrAsRepoInterface<OutputHandle>,
        AsRepoInterface<OutputHandle>,
        TagOrAsRepoInterface<OutputHandle> {

    private static final String CREATE_OPERATION = "create";
    private static final String TAG = "tag";

    private final String tag;
    private final String source;
    private final EventListener listener;

    public ImageImport(OkHttpClient client, Config config, String source) {
        this(client, config, source, null, NULL_LISTENER);
    }

    public ImageImport(OkHttpClient client, Config config, String source, String tag, EventListener listener) {
        super(client, config, IMAGES_RESOURCE, null, CREATE_OPERATION);
        this.tag = tag;
        this.source = source;
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
                    .header("X-Registry-Auth", java.util.Base64.getUrlEncoder().encodeToString(JSON_MAPPER.writeValueAsString(authConfig).getBytes("UTF-8")))
                    .post(RequestBody.create(MEDIA_TYPE_TEXT, EMPTY))
                    .url(sb.toString()).build();

            ImageImportHandle handle = new ImageImportHandle(config.getImagePushTimeout(), TimeUnit.MILLISECONDS, listener);
            client.newCall(request).enqueue(handle);
            return handle;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public TagOrAsRepoInterface<OutputHandle> usingListener(EventListener listener) {
        return new ImageImport(client, config, source, tag, listener);
    }

    @Override
    public AsRepoInterface<OutputHandle> withTag(String tag) {
        return new ImageImport(client, config, source, tag, listener);
    }
}
