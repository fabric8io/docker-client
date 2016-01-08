package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.NetworkResource;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.dsl.network.FiltersOrAllInterface;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkList extends OperationSupport implements FiltersOrAllInterface<List<NetworkResource>> {

    private static final String FILTERS = "filters";
    private static final String ALL = "all";

    private final Map<String,String[]> filters;

    public NetworkList(OkHttpClient client, Config config) {
        this(client, config, null);
    }

    public NetworkList(OkHttpClient client, Config config, Map<String, String[]> filters) {
        super(client, config, NETWORK_RESOURCE, null, JSON_OPERATION);
        this.filters = filters;
    }

    private List<NetworkResource> doList(Boolean all) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getOperationUrl().toString());
            sb.append(Q).append(ALL).append(EQUALS).append(all);

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
    public io.fabric8.docker.client.dsl.network.AllInterface<List<NetworkResource>> filters(String key, String value) {
        Map<String, String[]> newFilters = new HashMap<>(this.filters);
        newFilters.put(key, new String[]{value});
        return new NetworkList(client, config, newFilters);
    }

    @Override
    public List<NetworkResource> all() {
        return doList(true);
    }
}
