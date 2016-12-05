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
import okhttp3.Request;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.Utils;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.misc.EventsInterface;
import io.fabric8.docker.dsl.misc.ListFiltersInterface;
import io.fabric8.docker.dsl.misc.UntilFiltersListInterface;

import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EventOperationImpl extends OperationSupport implements
        EventsInterface,
        ListFiltersInterface<OutputHandle>,
        UntilFiltersListInterface<OutputHandle> {

    private static final String EVENTS_RESOURCE = "events";
    private static final String SINCE = "since";
    private static final String UNTIL = "until";
    private static final String FILTERS = "filters";

    private final String since;
    private final String until;
    private final Map<String, String[]> filters;

    public EventOperationImpl(OkHttpClient client, Config config) {
        this(client, config, null, null, new HashMap<String, String[]>());
    }

    public EventOperationImpl(OkHttpClient client, Config config, String since, String until, Map<String, String[]> filters) {
        super(client, config, EVENTS_RESOURCE);
        this.since = since;
        this.until = until;
        this.filters = filters;
    }


    @Override
    public OutputHandle list() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getOperationUrl())
                    .append(Q).append(FILTERS).append(EQUALS).append(JSON_MAPPER.writeValueAsString(filters));
            
            if (Utils.isNotNullOrEmpty(since)) {
                sb.append(A).append(SINCE).append(EQUALS).append(since);
            }

            if (Utils.isNotNullOrEmpty(until)) {
                sb.append(A).append(UNTIL).append(EQUALS).append(until);
            }


            Request request = new Request.Builder().get().url(sb.toString()).build();
            OkHttpClient clone = client.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).build();
            EventHandle handle = new EventHandle(new PipedOutputStream(), config.getRequestTimeout(), TimeUnit.MILLISECONDS);
            clone.newCall(request).enqueue(handle);
            return handle;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public ListFiltersInterface<OutputHandle> filters(String key, String value) {
        Map<String, String[]> newFilters = this.filters != null
                ? new HashMap<>(this.filters)
                : new HashMap<String, String[]>();

        newFilters.put(key, new String[]{value});
        return new EventOperationImpl(client, config, since, until, newFilters);
    }

    @Override
    public UntilFiltersListInterface<OutputHandle> since(String since) {
        return new EventOperationImpl(client, config, since, until, filters);
    }

    @Override
    public ListFiltersInterface<OutputHandle> until(String until) {
        return new EventOperationImpl(client, config, since, until, filters);
    }
}
