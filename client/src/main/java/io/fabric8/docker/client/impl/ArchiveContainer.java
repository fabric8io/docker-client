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

import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.URLUtils;
import io.fabric8.docker.dsl.container.DownloadFromHostResourceTarInputStreamInterface;
import io.fabric8.docker.dsl.container.NoOverwriteDirNonDownloadFromHostResourceTarInputStreamInterface;
import io.fabric8.docker.dsl.container.UploadToDownloadFromHostResourceTarInputStreamInterface;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static io.fabric8.docker.client.utils.ArchiveUtil.tar;

public class ArchiveContainer extends BaseContainerOperation implements
    NoOverwriteDirNonDownloadFromHostResourceTarInputStreamInterface<InputStream, Boolean>,
    DownloadFromHostResourceTarInputStreamInterface<InputStream, Boolean>,
    UploadToDownloadFromHostResourceTarInputStreamInterface<InputStream, Boolean> {

    private static final String ARCHIVE = "archive";

    private InputStream tarInputStream;

    private String uploadToPath;

    private boolean noOverwriteDirNonDir;

    public ArchiveContainer(OkHttpClient client, Config config, String name, String uploadToPath,
        InputStream tarInputStream, boolean noOverwriteDirNonDir) {
        super(client, config, name, ARCHIVE);
        this.uploadToPath = uploadToPath;
        this.tarInputStream = tarInputStream;
        this.noOverwriteDirNonDir = noOverwriteDirNonDir;
    }

    @Override
    public InputStream downloadFrom(String path) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(URLUtils.join(getResourceUrl().toString(), ARCHIVE));
            sb.append(Q).append("path").append(EQUALS).append(path);
            Request.Builder requestBuilder =
                new Request.Builder().get().url(new URL(sb.toString()));
            return handleResponseStream(requestBuilder, 200);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public NoOverwriteDirNonDownloadFromHostResourceTarInputStreamInterface<InputStream, Boolean> uploadTo(String path) {
        return new ArchiveContainer(client, config, name, path, tarInputStream, noOverwriteDirNonDir);
    }

    @Override
    public DownloadFromHostResourceTarInputStreamInterface<InputStream, Boolean> withNoOverwriteDirNonDir(
        boolean noOverwriteDirNonDir) {
        return new ArchiveContainer(client, config, name, uploadToPath, tarInputStream, noOverwriteDirNonDir);
    }

    @Override
    public Boolean withHostResource(String resource) {
        try {
            Path tempFile = createTempFile();
            tar(Paths.get(resource), tempFile);
            uploadResource(tempFile);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public Boolean withTarInputStream(InputStream tarInputStream) {
        try {
            Path tempFile = createTempFile();
            Files.copy(tarInputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            uploadResource(tempFile);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    private Path createTempFile() throws IOException {
        return Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir", "/tmp")), "docker", ".tar.bzip2");
    }

    private void uploadResource(Path tempFile) throws IOException, ExecutionException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append(URLUtils.join(getResourceUrl().toString(), ARCHIVE));
        sb.append(Q).append("path").append(EQUALS).append(uploadToPath);
        sb.append(A).append("noOverwriteDirNonDir").append(EQUALS).append(noOverwriteDirNonDir);

        RequestBody body = RequestBody.create(MEDIA_TYPE_BZIP2, tempFile.toFile());

        Request.Builder requestBuilder =
            new Request.Builder()
                .put(body)
                .url(new URL(sb.toString()));

        handleResponse(requestBuilder, 200);
    }
}
