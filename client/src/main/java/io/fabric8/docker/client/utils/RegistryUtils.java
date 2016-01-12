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

public class RegistryUtils {

    private static final String DOT = ".";
    private static final String COLON = ":";
    private static final String SEPARATOR = "/";

    public static AuthConfig getConfigForImage(String image, Config config) {
        String registry = extractRegistry(image);
        if (registry != null && config != null && config.getAuthConfigs().containsKey(registry)) {
            return config.getAuthConfigs().get(registry);
        }
        return null;
    }

    public static boolean isRegistry(String str) {
        return str.contains(DOT) || str.contains(COLON);
    }

    public static String extractRegistry(String image) {
        String[] parts = image.split(SEPARATOR);
        if (isRegistry(parts[0])) {
            return parts[0];
        } else {
            return null;
        }
    }
}
