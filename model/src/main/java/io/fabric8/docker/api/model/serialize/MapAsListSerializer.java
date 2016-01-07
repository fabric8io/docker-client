package io.fabric8.docker.api.model.serialize;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapAsListSerializer extends JsonSerializer<Map<String, String>> {

    @Override
    public void serialize(final Map<String, String> value, final JsonGenerator gen, final SerializerProvider provider) throws IOException, JsonProcessingException {
        List<String> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : value.entrySet()) {
            values.add(entry.getKey() + "=" + entry.getValue());
        }
        gen.writeObject(values);
    }
}