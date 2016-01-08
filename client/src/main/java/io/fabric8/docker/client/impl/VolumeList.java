package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.NetworkResource;
import io.fabric8.docker.api.model.Volume;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.dsl.volume.AllInterface;
import io.fabric8.docker.client.dsl.volume.FiltersOrAllInterface;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolumeList extends OperationSupport implements
        FiltersOrAllInterface<List<Volume>> {

    private static final String FILTERS = "filters";
    private static final String ALL = "all";

    private final Map<String,String[]> filters;

    public VolumeList(OkHttpClient client, Config config) {
        this(client, config, null);
    }

    public VolumeList(OkHttpClient client, Config config, Map<String, String[]> filters) {
        super(client, config, VOLUME_RESOURCE, null, JSON_OPERATION);
        this.filters = filters;
    }

    private List<Volume> doList(Boolean all) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getOperationUrl().toString());
            sb.append(Q).append(ALL).append(EQUALS).append(all);

            if (filters != null && !filters.isEmpty()) {
                sb.append(A).append(FILTERS).append(EQUALS)
                        .append(JSON_MAPPER.writeValueAsString(filters));
            }
            URL requestUrl = new URL(sb.toString());
            return handleList(requestUrl, Volume.class);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public AllInterface<List<Volume>> filters(String key, String value) {
        Map<String, String[]> newFilters = new HashMap<>(this.filters);
        newFilters.put(key, new String[]{value});
        return new VolumeList(client, config, newFilters);
    }

    @Override
    public List<Volume> all() {
        return doList(true);
    }
}
