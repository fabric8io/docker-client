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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Id",
        "Name",
        "Image",
        "ImageID",
        "Command",
        "Created",
        "Ports",
        "SizeRw",
        "SizeRootFS",
        "Labels",
        "Status",
        "HostConfig",
        "SummaryNetworkSettings"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@ToString
@EqualsAndHashCode
@Buildable(editableEnabled = true, validationEnabled = true, generateBuilderPackage = true, builderPackage = "io.fabric8.docker.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class Container implements Serializable {

    @JsonProperty("Id")
    private String id;
    @JsonProperty("Names")
    private List<String> names = new ArrayList<String>();
    @JsonProperty("Image")
    private String image;
    @JsonProperty("ImageID")
    private String  imageId;
    @JsonProperty("Command")
    private String  command;
    @JsonProperty("Created")
    private Long  created;
    @JsonProperty("Ports")
    private List<Port> ports;
    @JsonProperty("SizeRw")
    private Long sizeRw;
    @JsonProperty("SizeRootFS")
    private Long sizeRootFs;
    @JsonProperty("Labels")
    private Map<String,String> labels;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("HostConfig")
    private ContainerHostConfig hostConfig;
    @JsonProperty("SummaryNetworkSettings")
    private NetworkSettings summaryNetworkSettings;

    public Container() {
    }

    public Container(String id, List<String> names, String image, String imageId, String command, Long created, List<Port> ports, Long sizeRw, Long sizeRootFs, Map<String, String> labels, String status, ContainerHostConfig hostConfig, NetworkSettings summaryNetworkSettings) {
        this.id = id;
        this.names = names;
        this.image = image;
        this.imageId = imageId;
        this.command = command;
        this.created = created;
        this.ports = ports;
        this.sizeRw = sizeRw;
        this.sizeRootFs = sizeRootFs;
        this.labels = labels;
        this.status = status;
        this.hostConfig = hostConfig;
        this.summaryNetworkSettings = summaryNetworkSettings;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public List<Port> getPorts() {
        return ports;
    }

    public void setPorts(List<Port> ports) {
        this.ports = ports;
    }

    public Long getSizeRw() {
        return sizeRw;
    }

    public void setSizeRw(Long sizeRw) {
        this.sizeRw = sizeRw;
    }

    public Long getSizeRootFs() {
        return sizeRootFs;
    }

    public void setSizeRootFs(Long sizeRootFs) {
        this.sizeRootFs = sizeRootFs;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ContainerHostConfig getHostConfig() {
        return hostConfig;
    }

    public void setHostConfig(ContainerHostConfig hostConfig) {
        this.hostConfig = hostConfig;
    }

    public NetworkSettings getSummaryNetworkSettings() {
        return summaryNetworkSettings;
    }

    public void setSummaryNetworkSettings(NetworkSettings summaryNetworkSettings) {
        this.summaryNetworkSettings = summaryNetworkSettings;
    }
}
