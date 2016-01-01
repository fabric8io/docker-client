package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.dsl.container.DownloadFromOrUploadToInterface;

import java.io.InputStream;
import java.io.OutputStream;

public class ContainerArchieve extends BaseContainerOperation  implements DownloadFromOrUploadToInterface<InputStream, OutputStream> {

    private static final String ARCHIVE = "archive";

    public ContainerArchieve(OkHttpClient client, Config config, String name) {
        super(client, config, name, ARCHIVE);
    }

    @Override
    public InputStream downloadFrom(String path) {
        return null;
    }

    @Override
    public OutputStream uploadTo(String path) {
        return null;
    }
}
