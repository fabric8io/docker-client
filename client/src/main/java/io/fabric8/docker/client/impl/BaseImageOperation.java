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

import okhttp3.OkHttpClient;
import io.fabric8.docker.client.Config;

public class BaseImageOperation extends OperationSupport {

    protected static final String IMAGES_RESOURCE = "images";

    protected static final String DEFAULT_TEMP_DIR = System.getProperty("tmp.dir", "/tmp");
    protected static final String DOCKER_PREFIX = "docker-";
    protected static final String BZIP2_SUFFIX = ".tar.bzip2";


    public BaseImageOperation(OkHttpClient client, Config config, String name, String operationType) {
        super(client, config, IMAGES_RESOURCE, name, operationType);
    }

    public BaseImageOperation(OkHttpClient client, Config config, String resource, String name, String operationType) {
        super(client, config, resource, name, operationType);
    }
}
