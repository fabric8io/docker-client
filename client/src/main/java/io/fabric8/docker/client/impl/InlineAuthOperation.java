package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.client.Config;

public class InlineAuthOperation extends OperationSupport {

    public InlineAuthOperation(OkHttpClient client, Config config) {
        super(client, config, "auth", null, null);
    }

    public Boolean auth(AuthConfig authConfig) {
        try {
            handleCreate(authConfig);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
