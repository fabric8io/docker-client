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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.docker.api.model.serialize.ExposedPortSerializer;
import io.fabric8.docker.api.model.serialize.MapAsListSerializer;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Hostname",
        "Domainname",
        "User",
        "Memory",
        "MemorySwap",
        "MemoryReservation",
        "KernelMemory",
        "CpuShares",
        "CpuPeriod",
        "CpuQuota",
        "Cpuset",
        "CpusetCpus",
        "CpusetMems",
        "BlkioWeight",
        "MemorySwappiness",
        "OomKillDisable",
        "AttachStdin",
        "AttachStdout",
        "AttachStderr",
        "Tty",
        "OpenStdin",
        "StdinOnce",
        "Env",
        "Cmd",
        "Entrypoint",
        "Image",
        "Labels",
        "Mounts",
        "WorkingDir",
        "NetworkDisabled",
        "ExposedPorts",
        "StopSignal",
        "HostConfig"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@ToString
@EqualsAndHashCode
@Buildable(editableEnabled = true, validationEnabled = true, generateBuilderPackage = true, builderPackage = "io.fabric8.docker.api.builder",
        inline = {
                @Inline(type = Doneable.class, prefix = "Doneable", value = "done"),
                @Inline(name = "InlineContainerCreate", type = Doneable.class, returnType = ContainerCreateResponse.class, value = "done")
        }
)
public class ContainerCreateRequest implements Serializable {

    private static final long serialVersionUID = 5464523908891656210L;

    @JsonProperty("Hostname")
    private String hostname;

    @JsonProperty("Domainname")
    private String domainname;

    @JsonProperty("User")
    private String user;

    @JsonProperty("Memory")
    private String memory;

    @JsonProperty("MemorySwap")
    private String memorySwap;

    @JsonProperty("MemoryReservation")
    private String memoryReservation;

    @JsonProperty("KernelMemory")
    private String kernelMemory;

    @JsonProperty("CpuShares")
    private String cpuShares;

    @JsonProperty("CpuPeriod")
    private String cpuPeriod;

    @JsonProperty("CpuQuota")
    private String cpuQuota;

    @JsonProperty("Cpuset")
    private String cpuset;

    @JsonProperty("CpusetCpus")
    private String cpusetCpus;

    @JsonProperty("CpusetMems")
    private String cpusetMems;

    @JsonProperty("BlkioWeight")
    private Integer blkioWeight;

    @JsonProperty("MemorySwappiness")
    private Integer memorySwappiness;

    @JsonProperty("OomKillDisable")
    private Boolean oomKillDisable;

    @JsonProperty("AttachStdin")
    private Boolean attachStdin;

    @JsonProperty("AttachStdout")
    private Boolean attachStdout;

    @JsonProperty("AttachStderr")
    private Boolean attachStderr;

    @JsonProperty("Tty")
    private Boolean tty;

    @JsonProperty("OpenStdin")
    private Boolean openStdin;

    @JsonProperty("StdinOnce")
    private Boolean stdinOnce;

    @JsonProperty("Env")
    @JsonSerialize(using = MapAsListSerializer.class)
    private Map<String, String> env;

    @JsonProperty("Cmd")
    private List<String> cmd;

    @JsonProperty("Entrypoint")
    private String entrypoint;

    @JsonProperty("Image")
    private String image;

    @JsonProperty("Labels")
    private Map<String, String> labels;

    @JsonProperty("Mounts")
    private Map<String, String> mounts;

    @JsonProperty("WorkingDir")
    private String workingDir;

    @JsonProperty("NetworkDisabled")
    private Boolean networkDisabled;

    @JsonProperty("MacAddress")
    private String macAddress;

    @JsonProperty("ExposedPorts")
    @JsonSerialize(using = ExposedPortSerializer.class)
    private Map<Integer, Protocol> exposedPorts;

    @JsonProperty("StopSignal")
    private String stopSignal;

    @JsonProperty("HostConfig")
    private HostConfig hostConfig;

    public ContainerCreateRequest(String cpuset, String hostname, String domainname, String user, String memory, String memorySwap, String memoryReservation, String kernelMemory, String cpuShares, String cpuPeriod, String cpuQuota, String cpusetCpus, String cpusetMems, Integer blkioWeight, Integer memorySwappiness, Boolean oomKillDisable, Boolean attachStdin, Boolean attachStdout, Boolean attachStderr, Boolean tty, Boolean openStdin, Boolean stdinOnce, Map<String, String> env, List<String> cmd, String entrypoint, String image, Map<String, String> labels, Map<String, String> mounts, String workingDir, Boolean networkDisabled, String macAddress, Map<Integer, Protocol> exposedPorts, String stopSignal, HostConfig hostConfig) {
        this.cpuset = cpuset;
        this.hostname = hostname;
        this.domainname = domainname;
        this.user = user;
        this.memory = memory;
        this.memorySwap = memorySwap;
        this.memoryReservation = memoryReservation;
        this.kernelMemory = kernelMemory;
        this.cpuShares = cpuShares;
        this.cpuPeriod = cpuPeriod;
        this.cpuQuota = cpuQuota;
        this.cpusetCpus = cpusetCpus;
        this.cpusetMems = cpusetMems;
        this.blkioWeight = blkioWeight;
        this.memorySwappiness = memorySwappiness;
        this.oomKillDisable = oomKillDisable;
        this.attachStdin = attachStdin;
        this.attachStdout = attachStdout;
        this.attachStderr = attachStderr;
        this.tty = tty;
        this.openStdin = openStdin;
        this.stdinOnce = stdinOnce;
        this.env = env;
        this.cmd = cmd;
        this.entrypoint = entrypoint;
        this.image = image;
        this.labels = labels;
        this.mounts = mounts;
        this.workingDir = workingDir;
        this.networkDisabled = networkDisabled;
        this.macAddress = macAddress;
        this.exposedPorts = exposedPorts;
        this.stopSignal = stopSignal;
        this.hostConfig = hostConfig;
    }

    public String getCpuset() {
        return cpuset;
    }

    public String getHostname() {
        return hostname;
    }

    public String getDomainname() {
        return domainname;
    }

    public String getUser() {
        return user;
    }

    public String getMemory() {
        return memory;
    }

    public String getMemorySwap() {
        return memorySwap;
    }

    public String getMemoryReservation() {
        return memoryReservation;
    }

    public String getKernelMemory() {
        return kernelMemory;
    }

    public String getCpuShares() {
        return cpuShares;
    }

    public String getCpuPeriod() {
        return cpuPeriod;
    }

    public String getCpuQuota() {
        return cpuQuota;
    }

    public String getCpusetCpus() {
        return cpusetCpus;
    }

    public String getCpusetMems() {
        return cpusetMems;
    }

    public Integer getBlkioWeight() {
        return blkioWeight;
    }

    public Integer getMemorySwappiness() {
        return memorySwappiness;
    }

    public Boolean getOomKillDisable() {
        return oomKillDisable;
    }

    public Boolean getAttachStdin() {
        return attachStdin;
    }

    public Boolean getAttachStdout() {
        return attachStdout;
    }

    public Boolean getAttachStderr() {
        return attachStderr;
    }

    public Boolean getTty() {
        return tty;
    }

    public Boolean getOpenStdin() {
        return openStdin;
    }

    public Boolean getStdinOnce() {
        return stdinOnce;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public List<String> getCmd() {
        return cmd;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public String getImage() {
        return image;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public Map<String, String> getMounts() {
        return mounts;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public Boolean getNetworkDisabled() {
        return networkDisabled;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public Map<Integer, Protocol> getExposedPorts() {
        return exposedPorts;
    }

    public String getStopSignal() {
        return stopSignal;
    }

    public HostConfig getHostConfig() {
        return hostConfig;
    }
}
