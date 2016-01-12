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

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.Container;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.container.AllOrRunningOrFiltersInterface;
import io.fabric8.docker.dsl.container.BeforeOrSizeOrFiltersOrAllOrRunningInterface;
import io.fabric8.docker.dsl.container.LimitOrSinceOrBeforeOrSizeOrFiltersOrAllOrRunningInterface;
import io.fabric8.docker.dsl.container.SinceOrBeforeOrSizeOrFiltersOrAllOrRunningInterface;
import io.fabric8.docker.dsl.container.SizeOrFiltersOrAllOrRunningInterface;
import io.fabric8.docker.client.utils.Utils;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerList extends BaseContainerOperation implements
        LimitOrSinceOrBeforeOrSizeOrFiltersOrAllOrRunningInterface<List<Container>>,
        SizeOrFiltersOrAllOrRunningInterface<List<Container>>,
        BeforeOrSizeOrFiltersOrAllOrRunningInterface<List<Container>>,
        SinceOrBeforeOrSizeOrFiltersOrAllOrRunningInterface<List<Container>>,
        AllOrRunningOrFiltersInterface<List<Container>> {

    private final String before;
    private final String since;
    private final String size;
    private final Map<String, String[]> filters;
    private final int limit;


    public ContainerList(OkHttpClient client, Config config, String before, String since, String size, Map<String, String[]> filters, int limit) {
        super(client, config, null, JSON);
        this.before = before;
        this.since = since;
        this.size = size;
        this.filters = filters;
        this.limit = limit;
    }


    private List<Container> doList(Boolean all) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getOperationUrl().toString());
            sb.append("?all=").append(all);

            if (Utils.isNotNullOrEmpty(before)) {
                sb.append("&before=").append(before);
            }

            if (Utils.isNotNullOrEmpty(since)) {
                sb.append("&since=").append(since);
            }

            if (limit > 0) {
                sb.append("&limit=").append(limit);
            }

            if (filters != null && !filters.isEmpty()) {
                sb.append("&filters=");
               /* boolean first = true;
                for (Map.Entry<String,String> entry : filters.entrySet()) {
                    if (first) {
                        first=false;
                    } else {
                        sb.append(",");
                    }
                   sb.append(entry.getKey()).append(":").append(entry.getValue());
                }*/
                sb.append(JSON_MAPPER.writeValueAsString(filters));
            }

            URL requestUrl = new URL(sb.toString());
            return handleList(requestUrl, Container.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public List<Container> all() {
        return doList(true);
    }

    @Override
    public List<Container> running() {
        return doList(false);
    }


    @Override
    public SizeOrFiltersOrAllOrRunningInterface<List<Container>> before(String before) {
        return new ContainerList(client, config, before, since, size, filters, limit);
    }

    @Override
    public AllOrRunningOrFiltersInterface<List<Container>> filters(String key, String value) {
        Map<String, String[]> newFilters = new HashMap<>(this.filters);
        newFilters.put(key, new String[]{value});
        return new ContainerList(client, config, before, since, size, newFilters, limit);
    }

    @Override
    public SinceOrBeforeOrSizeOrFiltersOrAllOrRunningInterface<List<Container>> limit(int limit) {
        return new ContainerList(client, config, before, since, size, filters, limit);
    }

    @Override
    public BeforeOrSizeOrFiltersOrAllOrRunningInterface<List<Container>> since(String since) {
        return new ContainerList(client, config, before, since, size, filters, limit);
    }

    @Override
    public AllOrRunningOrFiltersInterface<List<Container>> size(String id) {
        return new ContainerList(client, config, before, since, size, filters, limit);
    }
}
