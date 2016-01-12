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

package io.fabric8.docker.api.model;

public class InlineVolumeCreate extends VolumeCreateRequestFluentImpl<InlineVolumeCreate> implements Doneable<Volume>, VolumeCreateRequestFluent<InlineVolumeCreate> {

    private final VolumeCreateRequestBuilder builder;
    private final Callback<VolumeCreateRequest, Volume> callback;

    public InlineVolumeCreate(Callback<VolumeCreateRequest, Volume> callback) {
        this.callback = callback;
        this.builder = new VolumeCreateRequestBuilder(this);
    }


    public Volume done() {
        return callback.call(builder.build());
    }

}
