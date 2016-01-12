
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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "Config",
    "ContainerJSONBase",
    "Mounts",
    "NetworkSettings"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@ToString
@EqualsAndHashCode
@Buildable(editableEnabled = true, validationEnabled = true, generateBuilderPackage = true, builderPackage = "io.fabric8.docker.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class ContainerInfo extends ContainerJSONBase {

    /**
     *
     *
     */
    @JsonProperty("Config")
    @Valid
    private Config Config;

    @JsonProperty("Mounts")
    @Valid
    private List<MountPoint> Mounts = new ArrayList<MountPoint>();
    /**
     *
     *
     */
    @JsonProperty("NetworkSettings")
    @Valid
    private io.fabric8.docker.api.model.NetworkSettings NetworkSettings;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public ContainerInfo() {
    }

    public ContainerInfo(String AppArmorProfile, List<String> Args, String Created, String Driver, String ExecDriver, List<String> ExecIDs, GraphDriverData GraphDriver, io.fabric8.docker.api.model.HostConfig HostConfig, String HostnamePath, String HostsPath, String Id, String Image, String LogPath, String MountLabel, String Name, String Path, String ProcessLabel, String ResolvConfPath, Integer RestartCount, Long SizeRootFs, Long SizeRw, ContainerState State, Config config, List<MountPoint> mounts, NetworkSettings networkSettings) {
        super(AppArmorProfile, Args, Created, Driver, ExecDriver, ExecIDs, GraphDriver, HostConfig, HostnamePath, HostsPath, Id, Image, LogPath, MountLabel, Name, Path, ProcessLabel, ResolvConfPath, RestartCount, SizeRootFs, SizeRw, State);
        Config = config;
        Mounts = mounts;
        NetworkSettings = networkSettings;
        this.additionalProperties = additionalProperties;
    }

    /**
     * 
     * 
     * @return
     *     The Config
     */
    @JsonProperty("Config")
    public Config getConfig() {
        return Config;
    }

    /**
     * 
     * 
     * @param Config
     *     The Config
     */
    @JsonProperty("Config")
    public void setConfig(Config Config) {
        this.Config = Config;
    }


    /**
     * 
     * 
     * @return
     *     The Mounts
     */
    @JsonProperty("Mounts")
    public List<MountPoint> getMounts() {
        return Mounts;
    }

    /**
     * 
     * 
     * @param Mounts
     *     The Mounts
     */
    @JsonProperty("Mounts")
    public void setMounts(List<MountPoint> Mounts) {
        this.Mounts = Mounts;
    }

    /**
     * 
     * 
     * @return
     *     The NetworkSettings
     */
    @JsonProperty("NetworkSettings")
    public io.fabric8.docker.api.model.NetworkSettings getNetworkSettings() {
        return NetworkSettings;
    }

    /**
     * 
     * 
     * @param NetworkSettings
     *     The NetworkSettings
     */
    @JsonProperty("NetworkSettings")
    public void setNetworkSettings(io.fabric8.docker.api.model.NetworkSettings NetworkSettings) {
        this.NetworkSettings = NetworkSettings;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
