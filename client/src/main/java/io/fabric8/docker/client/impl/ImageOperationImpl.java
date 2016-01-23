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

import com.fasterxml.jackson.databind.JavaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import io.fabric8.docker.api.model.Image;
import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.api.model.ImageHistory;
import io.fabric8.docker.api.model.ImageInspect;
import io.fabric8.docker.api.model.SearchResult;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.InputStreamPumper;
import io.fabric8.docker.client.utils.URLUtils;
import io.fabric8.docker.api.model.Callback;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.image.FilterOrFiltersOrAllImagesOrEndImagesInterface;
import io.fabric8.docker.dsl.image.ImageInspectOrHistoryOrPushOrTagOrDeleteOrGetOrLoadInterface;
import io.fabric8.docker.dsl.image.ImageInterface;
import io.fabric8.docker.dsl.image.RepositoryNameOrSupressingVerboseOutputOrNoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrRedirectingWritingOutputOrFromPathInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrRedirectingWritingOutputOrTagOrAsRepoInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrRedirectingWritingOutputOrTagOrFromImageInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ImageOperationImpl extends OperationSupport implements ImageInterface {

    private static final JavaType IMAGE_SEARCH_RESULT_LIST = JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, SearchResult.class);

    private static final String TERM = "term";


    public ImageOperationImpl(OkHttpClient client, Config config) {
        super(client, config, IMAGES_RESOURCE, null, null);
    }

    @Override
    public RepositoryNameOrSupressingVerboseOutputOrNoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrRedirectingWritingOutputOrFromPathInterface<OutputHandle> build() {
        return new ImageBuild(client, config);
    }

    @Override
    public FilterOrFiltersOrAllImagesOrEndImagesInterface<List<Image>> list() {
        return new ImageList(client,config, null, new HashMap<String,String[]>());
    }

    @Override
    public ImageInspectOrHistoryOrPushOrTagOrDeleteOrGetOrLoadInterface<ImageInspect, List<ImageHistory>, OutputHandle, Boolean, ImageDelete, InputStream> withName(String name) {
        return new ImageNamedOperationImpl(client, config, name);
    }

    @Override
    public UsingListenerOrRedirectingWritingOutputOrTagOrAsRepoInterface<OutputHandle> importFrom(String source) {
        return new ImageImport(client, config, source);
    }

    @Override
    public UsingListenerOrRedirectingWritingOutputOrTagOrFromImageInterface<OutputHandle> pull() {
        return new ImagePull(client, config);
    }

    @Override
    public List<SearchResult> search(String term) {
        try {
            StringBuilder sb = new StringBuilder()
                    .append(getOperationUrl(SEARCH_OPERATION))
                    .append(Q).append(TERM).append(EQUALS).append(term);

            Request request = new Request.Builder().get().url(new URL(sb.toString())).get().build();
            Response response = null;
            try {
                OkHttpClient clone = client.clone();
                clone.setReadTimeout(config.getImageSearchTimeout(), TimeUnit.MILLISECONDS);
                response = clone.newCall(request).execute();
                assertResponseCodes(request, response, 200);
            } catch (Exception e) {
                throw requestException(request, e);
            }
            return JSON_MAPPER.readValue(response.body().byteStream(), IMAGE_SEARCH_RESULT_LIST);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public InputStream get() {
        try {
            StringBuilder sb = new StringBuilder()
                    .append(getOperationUrl("get"));
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
                    .append(URLUtils.join(getRootUrl().toString(), "load").toString());


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
            File tempFile = Files.createTempFile(Paths.get(DEFAULT_TEMP_DIR), TEMP_PREFIX, TEMP_SUFFIX).toFile();
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
}
