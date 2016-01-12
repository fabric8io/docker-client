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
