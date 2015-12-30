package io.fabric8.docker.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@ToString
@EqualsAndHashCode
@Buildable(editableEnabled = true, validationEnabled = true, generateBuilderPackage = true, builderPackage = "io.fabric8.docker.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class ContainerHostConfig {
    @JsonProperty("NetworkMode")
    private String networkMode;

    protected ContainerHostConfig() {
    }

    protected ContainerHostConfig(String networkMode) {
        this.networkMode = networkMode;
    }

    public String getNetworkMode() {
        return networkMode;
    }

    public void setNetworkMode(String networkMode) {
        this.networkMode = networkMode;
    }
}