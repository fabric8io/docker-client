package io.fabric8.docker.client.impl;


import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.Utils;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.misc.EventsInterface;
import io.fabric8.docker.dsl.misc.FiltersOrListInterface;
import io.fabric8.docker.dsl.misc.ListInterface;
import io.fabric8.docker.dsl.misc.UntilOrFiltersOrListInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EventOperationImpl extends OperationSupport implements
        EventsInterface,
        FiltersOrListInterface<OutputHandle>,
        UntilOrFiltersOrListInterface<OutputHandle> {

    private static final String EVENTS_RESOURCE = "events";
    private static final String SINCE = "since";
    private static final String UNTIL = "until";
    private static final String FILTERS = "filters";

    private final String since;
    private final String until;
    private final Map<String, String[]> filters;

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
            EventHandle handle = new EventHandle(config.getRequestTimeout(), TimeUnit.MILLISECONDS);
            client.newCall(request).enqueue(handle);
            return handle;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public ListInterface<OutputHandle> filters(String key, String value) {
        Map<String, String[]> newFilters = new HashMap<>(this.filters);
        newFilters.put(key, new String[]{value});
        return new EventOperationImpl(client, config, since, until, newFilters);
    }

    @Override
    public UntilOrFiltersOrListInterface<OutputHandle> since(String since) {
        return new EventOperationImpl(client, config, since, until, filters);
    }

    @Override
    public FiltersOrListInterface<OutputHandle> until(String until) {
        return new EventOperationImpl(client, config, since, until, filters);
    }
}
