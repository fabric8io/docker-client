package io.fabric8.docker.api.model.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.fabric8.docker.api.model.Protocol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExposedPortSerializer  extends JsonSerializer<Map<Integer, Protocol>>  {

    @Override
    public void serialize(Map<Integer, Protocol> value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<Integer, Protocol> entry:value.entrySet()) {
            values.put(entry.getKey() + "/" + entry.getValue().name().toLowerCase(), new Object());
        }
        gen.writeObject(values);
    }
}
