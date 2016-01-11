package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.image.TagOrToRegistryInterface;
import io.fabric8.docker.dsl.image.ToRegistryInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrTagOrToRegistryInterface;
import io.fabric8.docker.client.utils.RegistryUtils;
import io.fabric8.docker.client.utils.Utils;

import java.util.concurrent.TimeUnit;

public class ImagePush extends OperationSupport implements
        UsingListenerOrTagOrToRegistryInterface<OutputHandle>,
        TagOrToRegistryInterface<OutputHandle> {

    private static final String TAG = "tag";

    private final String tag;
    private final EventListener listener;

    public ImagePush(OkHttpClient client, Config config, String name) {
        this(client, config, name, null, NULL_LISTENER);
    }

    public ImagePush(OkHttpClient client, Config config, String name, String tag, EventListener listener) {
        super(client, config, IMAGES_RESOURCE, name, PUSH_OPERATION);
        this.tag = tag;
        this.listener = listener;
    }

    @Override
    public OutputHandle toRegistry() {
        try {
            StringBuilder sb = new StringBuilder().append(getOperationUrl(PUSH_OPERATION));
            if (Utils.isNotNullOrEmpty(tag)) {
                sb.append(Q).append(TAG).append(EQUALS).append(tag);
            }

            AuthConfig authConfig = RegistryUtils.getConfigForImage(name, config);
            Request request = new Request.Builder()
                    .header("X-Registry-Auth", java.util.Base64.getUrlEncoder().encodeToString(JSON_MAPPER.writeValueAsString(authConfig).getBytes("UTF-8")))
                    .post(RequestBody.create(MEDIA_TYPE_TEXT, EMPTY))
                    .url(sb.toString()).build();

            ImagePushHandle handle = new ImagePushHandle(config.getImagePushTimeout(), TimeUnit.MILLISECONDS, listener);
            client.newCall(request).enqueue(handle);
            return handle;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public ToRegistryInterface<OutputHandle> withTag(String tag) {
        return new ImagePush(client, config, name, tag, listener);
    }

    @Override
    public TagOrToRegistryInterface<OutputHandle> usingListener(EventListener listener) {
        return new ImagePush(client, config, name, tag, listener);
    }
}
