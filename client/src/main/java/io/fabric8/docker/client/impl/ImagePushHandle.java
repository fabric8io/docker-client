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

import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.client.ProgressEvent;
import io.fabric8.docker.client.utils.Utils;

import java.util.concurrent.TimeUnit;

public class ImagePushHandle extends EventHandle {

    private static final String SUCCESSFULLY_BUILT = "Successfully built";

    public ImagePushHandle(long duration, TimeUnit unit, EventListener listener) {
        super(duration, unit, listener);
    }

    @Override
    public boolean isSuccess(ProgressEvent event) {
        return Utils.isNotNullOrEmpty(event.getStream()) && event.getStream().startsWith(SUCCESSFULLY_BUILT);
    }

    @Override
    public boolean isFailure(ProgressEvent event) {
        return Utils.isNotNullOrEmpty(event.getError());
    }
}
