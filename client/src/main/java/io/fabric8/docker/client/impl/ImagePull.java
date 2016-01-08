package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.EventListener;
import io.fabric8.docker.client.OutputHandle;
import io.fabric8.docker.client.dsl.image.FromImageInterface;
import io.fabric8.docker.client.dsl.image.TagOrFromImageInterface;
import io.fabric8.docker.client.dsl.image.UsingListenerOrTagOrFromImageInterface;
import io.fabric8.docker.client.utils.RegistryUtils;
import io.fabric8.docker.client.utils.Utils;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class ImagePull extends OperationSupport implements
        UsingListenerOrTagOrFromImageInterface<OutputHandle>,
        TagOrFromImageInterface<OutputHandle> {

    private static final String CREATE_OPERATION = "create";
    private static final String FROM_IMAGE = "fromImage";
    private static final String TAG = "tag";

    private final String tag;
    private final EventListener listener;

    public ImagePull(OkHttpClient client, Config config) {
        this(client, config, null, NULL_LISTENER);
    }

    public ImagePull(OkHttpClient client, Config config, String tag, EventListener listener) {
        super(client, config, IMAGES_RESOURCE, null, CREATE_OPERATION);
        this.tag = tag;
        this.listener = listener;
    }

    @Override
    public FromImageInterface<OutputHandle> withTag(String tag) {
        return new ImagePull(client, config, tag, listener);
    }

    @Override
    public TagOrFromImageInterface<OutputHandle> usingListener(EventListener listener) {
        return new ImagePull(client, config, tag, listener);
    }

    @Override
    public OutputHandle fromImage(String image) {
        try {
            StringBuilder sb = new StringBuilder().append(getOperationUrl(CREATE_OPERATION))
                    .append(Q).append(FROM_IMAGE).append(EQUALS).append(image);

            if (Utils.isNotNullOrEmpty(tag)) {
                sb.append(A).append(TAG).append(EQUALS).append(tag);
            }

            AuthConfig authConfig = RegistryUtils.getConfigForImage(image, config);
            if (authConfig == null && !config.getAuthConfigs().isEmpty()) {
                authConfig = config.getAuthConfigs().values().iterator().next();
            }
            Request request = new Request.Builder()
                    .header("X-Registry-Auth", Base64.getUrlEncoder().encodeToString(JSON_MAPPER.writeValueAsString(authConfig).getBytes("UTF-8")))
                    .post(RequestBody.create(MEDIA_TYPE_TEXT, EMPTY))
                    .url(sb.toString()).build();

            ImagePullHandle handle = new ImagePullHandle(config.getImagePushTimeout(), TimeUnit.MILLISECONDS, listener);
            client.newCall(request).enqueue(handle);
            return handle;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }
}
