#
# Copyright (C) 2011 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

SHELL := /bin/bash
tag := $(shell cat .docker-version)

prepare:
	-godep restore
	pushd $(GOPATH)/src/github.com/docker/docker/ &&  \
		sh hack/make/.go-autogen && \
		popd && \
		godep restore


build:
	CGO_ENABLED=0 godep go build -a ./cmd/generate/generate.go
	./generate > ../model/src/main/resources/schema/docker-schema.json
#	cd ../model/ && mvn clean install

update-deps:
	echo $(tag) > .docker-version && \
		pushd $(GOPATH)/src/github.com/docker/docker/api && \
		git fetch origin && \
		git checkout -B $(tag) refs/tags/$(tag) && \
		godep restore && \
		popd && \
		godep save cmd/generate/generate.go && \
		godep update ...
