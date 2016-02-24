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

package io.fabric8.docker.client.utils;

import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.client.Config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class RegistryUtils {

    private static final String DOT = ".";
    private static final String COLON = ":";
    private static final String LOCALHOST = "localhost";
    private static final String SEPARATOR = "/";

    public static AuthConfig getConfigForImage(String image, Config config) {
        String registry = extractRegistry(image);
        AuthConfig authConfig = null;
        if (registry != null && config != null && config.getAuthConfigs() != null) {
            String registryKey = getRegistryKey(registry, config);
            authConfig = config.getAuthConfigs().get(registryKey);
        }
        if (authConfig != null) {
            authConfig.setAuth("");
            if (Utils.isNullOrEmpty(authConfig.getServeraddress())) {
                authConfig.setServeraddress(registry);
            }
        }
        return authConfig;
    }

    public static boolean hasRegistry(String image) {
        String registry = extractRegistry(image);
        return registry != null && !registry.isEmpty();
    }

    public static String extractRegistry(String image) {
        String[] parts = image.split(SEPARATOR);
        if (isRegistry(parts[0])) {
            return parts[0];
        } else {
            return Config.DEFAULT_INDEX;
        }
    }

    public static boolean isRegistry(String str) {
        return str.contains(DOT) || str.contains(COLON) || LOCALHOST.equals(str);
    }

    public static String getRegistryKey(String registry, Config config) {
        if (config != null && config.getAuthConfigs() != null) {

            //1st the happy path
            if (config.getAuthConfigs().containsKey(registry)) {
                return registry;
            }

            //2nd try to match using hostname
            for (Map.Entry<String, AuthConfig> entry : config.getAuthConfigs().entrySet()) {
                String key = entry.getKey();
                if (convertToHostName(key).equals(registry)) {
                    return key;
                }
            }

            //3rd try to match using domainname
            for (Map.Entry<String, AuthConfig> entry : config.getAuthConfigs().entrySet()) {
                String key = entry.getKey();
                if (convertToHostName(key).endsWith(registry)) {
                    return key;
                }
            }
        }
        return Config.DOCKER_AUTH_FALLBACK_KEY;
    }


    /**
     * Equivalent of docker's convert to hostname:
     * https://github.com/docker/docker/blob/master/registry/auth.go#L232
     * @param str The string to convert to hostname.
     * @return
     */
    private static String convertToHostName(String str) {
        try {
            URL u = new URL(str);
            return u.getHost();
        } catch (MalformedURLException e) {
            return str;
        }
    }
}
