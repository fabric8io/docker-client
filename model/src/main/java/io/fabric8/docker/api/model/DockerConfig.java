package io.fabric8.docker.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class DockerConfig {

    @JsonProperty private Map<String, AuthConfig> auths;

    public DockerConfig() {
        this(new HashMap<String, AuthConfig>());
    }

    public DockerConfig(Map<String, AuthConfig> auths) {
        this.auths = auths;
    }

    public Map<String, AuthConfig> getAuths() {
        return auths;
    }

    public void setAuths(Map<String, AuthConfig> auths) {
        this.auths = auths;
    }
}
