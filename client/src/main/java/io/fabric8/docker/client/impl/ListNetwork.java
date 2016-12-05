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
import io.fabric8.docker.api.model.NetworkResource;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.network.AllFiltersInterface;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListNetwork extends BaseNetworkOperation implements AllFiltersInterface<List<NetworkResource>> {

    private static final String FILTERS = "filters";
    private static final String ALL = "all";

    private final Map<String,String[]> filters;

    public ListNetwork(OkHttpClient client, Config config) {
        this(client, config, null);
    }

    public ListNetwork(OkHttpClient client, Config config, Map<String, String[]> filters) {
        super(client, config, null, EMPTY);
        this.filters = filters;
    }

    private List<NetworkResource> doList(Boolean all) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getOperationUrl().toString());

            if (filters != null && !filters.isEmpty()) {
                sb.append(A).append(FILTERS).append(EQUALS)
                        .append(JSON_MAPPER.writeValueAsString(filters));
            }
            URL requestUrl = new URL(sb.toString());
            return handleList(requestUrl, NetworkResource.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public AllFiltersInterface<List<NetworkResource>> filters(String key, String value) {
        Map<String, String[]> newFilters = this.filters != null
                ? new HashMap<>(this.filters)
                : new HashMap<String, String[]>();

        newFilters.put(key, new String[]{value});
        return new ListNetwork(client, config, newFilters);
    }

    @Override
    public List<NetworkResource> all() {
        return doList(true);
    }
}
