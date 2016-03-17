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
import com.squareup.okhttp.Response;
import io.fabric8.docker.api.model.Callback;
import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.api.model.ImageHistory;
import io.fabric8.docker.api.model.ImageInspect;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.InputStreamPumper;
import io.fabric8.docker.client.utils.URLUtils;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.image.ForceOrAndPruneOrNoPruneInterface;
import io.fabric8.docker.dsl.image.ImageInspectOrPullOrHistoryOrPushOrTagOrDeleteOrGetOrLoadInterface;
import io.fabric8.docker.dsl.image.InRepositoryOrForceOrTagNameInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrRedirectingWritingOutputOrTagOrFromRegistryInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrRedirectingWritingOutputOrTagOrToRegistryOrForceInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ImageNamedOperationImpl extends BaseImageOperation implements
        ImageInspectOrPullOrHistoryOrPushOrTagOrDeleteOrGetOrLoadInterface<ImageInspect, OutputHandle, List<ImageHistory>, Boolean, List<ImageDelete>, InputStream> {

    private static final String HISTORY_OPERATION = "history";
    private static final String INSPECT_OPERATION = "json";
    private static final String GET_OPERATION = "get";
    private static final String LOAD_OPERATION = "load";

    public ImageNamedOperationImpl(OkHttpClient client, Config config, String name) {
        super(client, config, name, null);
    }

    @Override
    public ForceOrAndPruneOrNoPruneInterface<List<ImageDelete>> delete() {
        return new DeleteImage(client, config, name);
    }

    @Override
    public List<ImageHistory> history() {
        try {
            return handleList(getOperationUrl(HISTORY_OPERATION), ImageHistory.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public ImageInspect inspect() {
        try {
            return handleGet(getOperationUrl(INSPECT_OPERATION), ImageInspect.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }


    @Override
    public InRepositoryOrForceOrTagNameInterface<Boolean> tag() {
        return new TagImage(client, config, name);
    }

    @Override
    public UsingListenerOrRedirectingWritingOutputOrTagOrToRegistryOrForceInterface<OutputHandle> push() {
        return new PushImage(client, config, name);
    }

    @Override
    public InputStream get() {
        try {
            StringBuilder sb = new StringBuilder()
                    .append(getOperationUrl(GET_OPERATION));
            Request request = new Request.Builder()
                    .get()
                    .url(sb.toString()).build();
            Response response = client.newCall(request).execute();
            return response.body().byteStream();
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    public Boolean load(String path) {
        try {
            StringBuilder sb = new StringBuilder()
                    .append(URLUtils.join(getRootUrl().toString(), LOAD_OPERATION).toString());


            RequestBody body = RequestBody.create(MEDIA_TYPE_TAR, new File(path));
            Request request = new Request.Builder()
                    .post(body)
                    .url(sb.toString()).build();
            Response response = client.newCall(request).execute();
            return response.isSuccessful();
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean load(InputStream inputStream) {
        try {
            File tempFile = Files.createTempFile(Paths.get(DEFAULT_TEMP_DIR), DOCKER_PREFIX, BZIP2_SUFFIX).toFile();
            try (final FileOutputStream fout = new FileOutputStream(tempFile)) {

                InputStreamPumper pumper = new InputStreamPumper(inputStream, new Callback<byte[], Void>() {
                    @Override
                    public Void call(byte[] input) {
                        try {
                            fout.write(input);
                        } catch (IOException e) {
                            throw DockerClientException.launderThrowable(e);
                        }
                        return null;
                    }
                });
                pumper.run();
                pumper.close();
            }
            return load(tempFile.getAbsolutePath());
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public UsingListenerOrRedirectingWritingOutputOrTagOrFromRegistryInterface<OutputHandle> pull() {
        return new PullImage(client,config,name);
    }
}
