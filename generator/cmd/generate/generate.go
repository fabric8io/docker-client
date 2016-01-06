/**
 * Copyright (C) 2011 Red Hat, Inc.
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
 */
package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"reflect"
	"strings"
	"time"

	api "github.com/docker/docker/api/types"
	"github.com/docker/docker/daemon/network"
	"github.com/docker/docker/pkg/nat"
	"github.com/docker/docker/pkg/stringutils"
	"github.com/docker/docker/registry"
	"github.com/docker/docker/runconfig"
	"github.com/docker/docker/cliconfig"
	"github.com/fabric8io/docker-client/generator/pkg/schemagen"
)

type Schema struct {
	Address                     network.Address
	IPAM                        network.IPAM
	IPAMConfig                  network.IPAMConfig
	EndpointSettings            network.EndpointSettings
	ServiceConfig               registry.ServiceConfig
	IndexInfo                   registry.IndexInfo
	SearchResult                registry.SearchResult
	SearchResults               registry.SearchResults
	LogConfig                   runconfig.LogConfig
	HostConfig                  runconfig.HostConfig
	StrSlice                    stringutils.StrSlice
	ContainerCreateResponse     api.ContainerCreateResponse
	ContainerExecCreateResponse api.ContainerExecCreateResponse
	AuthResponse                api.AuthResponse
	ContainerWaitResponse       api.ContainerWaitResponse
	ContainerCommitResponse     api.ContainerCommitResponse
	ContainerChange             api.ContainerChange
	ImageHistory                api.ImageHistory
	ImageDelete                 api.ImageDelete
	Image                       api.Image
	GraphDriverData             api.GraphDriverData
	ImageInspect                api.ImageInspect
	Port                        api.Port
	CopyConfig                  api.CopyConfig
	ContainerPathStat           api.ContainerPathStat
	ContainerProcessList        api.ContainerProcessList
	Version                     api.Version
	Info                        api.Info
	ExecStartCheck              api.ExecStartCheck
	ContainerJSONBase           api.ContainerJSONBase
	ContainerState              api.ContainerState
	NetworkSettings             api.NetworkSettings
	NetworkSettingsBase         api.NetworkSettingsBase
	DefaultNetworkSettings      api.DefaultNetworkSettings
	MountPoint                  api.MountPoint
	Volume                      api.Volume
	VolumesListResponse         api.VolumesListResponse
	VolumeCreateRequest         api.VolumeCreateRequest
	NetworkResource             api.NetworkResource
	EndpointResource            api.EndpointResource
	NetworkCreate               api.NetworkCreate
	NetworkCreateResponse       api.NetworkCreateResponse
	NetworkConnect              api.NetworkConnect
	NetworkDisconnect           api.NetworkDisconnect
	Stats                       api.Stats
	AuthConfig					cliconfig.AuthConfig
	PortBinding                 nat.PortBinding
}

func main() {
	packages := []schemagen.PackageDescriptor{
		{"github.com/docker/docker/api/types", "io.fabric8.docker.api.model", "docker_"},
		{"github.com/docker/docker/api/types/network", "io.fabric8.docker.api.model", "docker_network_"},
		{"github.com/docker/docker/registry", "io.fabric8.docker.api.model", "docker_registry_"},
		{"github.com/docker/docker/runconfig", "io.fabric8.docker.api.model", "docker_runconfig_"},
		{"github.com/docker/docker/cliconfig", "io.fabric8.docker.api.model", "docker_cliconfig_"},
		{"github.com/docker/docker/daemon/network", "io.fabric8.docker.api.model", "docker_network_"},
		{"github.com/docker/docker/pkg/nat", "io.fabric8.docker.api.model", "docker_nat_"},
		{"github.com/docker/docker/pkg/stringutils", "io.fabric8.docker.api.model", "docker_stringutils_"},
		{"github.com/docker/docker/pkg/ulimit", "io.fabric8.docker.api.model", "docker_ulimit_"},
	}

	typeMap := map[reflect.Type]reflect.Type{
		reflect.TypeOf(time.Time{}): reflect.TypeOf(""),
		reflect.TypeOf(struct{}{}):  reflect.TypeOf(""),
	}
	schema, err := schemagen.GenerateSchema(reflect.TypeOf(Schema{}), packages, typeMap)
	if err != nil {
		fmt.Errorf("An error occurred: %v", err)
		return
	}

	b, err := json.Marshal(&schema)
	if err != nil {
		log.Fatal(err)
	}
	result := string(b)
	result = strings.Replace(result, "\"additionalProperty\":", "\"additionalProperties\":", -1)
	var out bytes.Buffer
	err = json.Indent(&out, []byte(result), "", "  ")
	if err != nil {
		log.Fatal(err)
	}

	fmt.Println(out.String())
}
