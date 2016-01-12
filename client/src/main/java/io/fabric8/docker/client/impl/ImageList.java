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
import io.fabric8.docker.api.model.Image;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.image.AllImagesOrEndImagesOrFiltersInterface;
import io.fabric8.docker.dsl.image.FilterOrFiltersOrAllImagesOrEndImagesInterface;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageList extends OperationSupport implements
        FilterOrFiltersOrAllImagesOrEndImagesInterface<List<Image>>,
        AllImagesOrEndImagesOrFiltersInterface<List<Image>> {

    private static final String FILTER = "filter";
    private static final String FILTERS = "filters";
    private static final String ALL = "all";

    private final String filter;
    private final Map<String,String[]> filters;

    public ImageList(OkHttpClient client, Config config,  String filter, Map<String, String[]> filters) {
        super(client, config, "images", null, "json");
        this.filter = filter;
        this.filters = filters;
    }

    private List<Image> doList(Boolean all) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getOperationUrl().toString());
            sb.append("?").append(ALL).append("=").append(all);

            if (filters != null && !filters.isEmpty()) {
                sb.append("?").append(FILTERS).append("=")
                        .append(JSON_MAPPER.writeValueAsString(filters));
            }
            if (filter != null && !filters.isEmpty()) {
                sb.append("?").append(FILTER).append("=").append(filter);
            }

            URL requestUrl = new URL(sb.toString());
            return handleList(requestUrl, Image.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }
    @Override
    public List<Image> allImages() {
        return doList(true);
    }


    @Override
    public List<Image> endImages() {
        return doList(false);
    }

    @Override
    public AllImagesOrEndImagesOrFiltersInterface<List<Image>> filter(String filter) {
        return new ImageList(client, config, filter, filters);
    }

    @Override
    public AllImagesOrEndImagesOrFiltersInterface<List<Image>> filters(String key, String value) {
        Map<String, String[]> newFilters = new HashMap<>(this.filters);
        newFilters.put(key, new String[]{value});
        return new ImageList(client, config, filter, newFilters);
    }

}
