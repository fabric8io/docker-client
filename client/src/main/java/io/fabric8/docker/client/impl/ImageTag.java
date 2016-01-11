package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.image.ForceOrTagNameInterface;
import io.fabric8.docker.dsl.image.InRepositoryOrForceOrTagNameInterface;
import io.fabric8.docker.dsl.image.WithTagNameInterface;
import io.fabric8.docker.client.utils.Utils;
import okio.ByteString;

import java.net.URL;

public class ImageTag extends OperationSupport implements
        InRepositoryOrForceOrTagNameInterface<Boolean>,
        ForceOrTagNameInterface<Boolean> {

    private static final String FORCE = "force";
    private static final String REPOSITORY = "repo";

    private final String repository;
    private final Boolean force;

    public ImageTag(OkHttpClient client, Config config, String name) {
        this(client, config, name, null, false);
    }

    public ImageTag(OkHttpClient client, Config config, String name, String repository, Boolean force) {
        super(client, config, IMAGES_RESOURCE, name, TAG_OPERATION);
        this.repository = repository;
        this.force = force;
    }

    @Override
    public WithTagNameInterface<Boolean> force() {
        return new ImageTag(client, config, name, repository, true);
    }

    @Override
    public ForceOrTagNameInterface<Boolean> inRepository(String repository) {
        return new ImageTag(client, config, name, repository, force);
    }

    @Override
    public Boolean withTagName(String tagName) {
        try {
            StringBuilder sb = new StringBuilder()
                    .append(getOperationUrl())
                    .append(Q).append(TAG_OPERATION).append(EQUALS).append(tagName);

            sb.append(A).append(FORCE).append(EQUALS).append(force);

            if (Utils.isNotNullOrEmpty(repository)) {
                sb.append(A).append(REPOSITORY).append(EQUALS).append(repository);
            }



            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, ByteString.EMPTY);
            Request.Builder requestBuilder = new Request.Builder()
                    .post(body)
                    .url(new URL(sb.toString()));
            handleResponse(requestBuilder, 201);
            return true;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }
}
