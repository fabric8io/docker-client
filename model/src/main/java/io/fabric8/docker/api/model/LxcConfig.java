
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

package io.fabric8.docker.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = LxcConfig.LxcConfigDeserializer.class)
public class LxcConfig implements Serializable
{
    private List<KeyValuePair> values;

    public LxcConfig(List<KeyValuePair> values) {
        this.values = values;
    }

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


    public static class LxcConfigDeserializer extends JsonDeserializer<LxcConfig> {

        private static final String KEY = "Key";
        private static final String VALUE = "Value";

        public LxcConfigDeserializer() {
        }

        @Override
        public LxcConfig deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode node = jp.getCodec().readTree(jp);
            List<KeyValuePair> keyValuePairs = new ArrayList<>();

            if (node.isArray()) {
                for (int i=0; i < node.size();i++) {
                    JsonNode keyValueNode = node.get(i);
                    String key = keyValueNode.get(KEY).textValue();
                    String value = keyValueNode.get(VALUE).textValue();
                    keyValuePairs.add(new KeyValuePair(key, value));
                }
            } else if (node.isObject()) {
                Iterator<Map.Entry<String,JsonNode>> iterator = node.fields();
                while (iterator.hasNext()) {
                    Map.Entry<String, JsonNode> entry = iterator.next();
                    keyValuePairs.add(new KeyValuePair(entry.getKey(), entry.getValue().textValue()));
                }
            }
            return new LxcConfig(keyValuePairs);
        }
    }
}
