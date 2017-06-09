Docker Client
---

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.fabric8/docker-client/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/io.fabric8/docker-client/)
[![Javadocs](http://www.javadoc.io/badge/io.fabric8/docker-client.svg?color=blue)](http://www.javadoc.io/doc/io.fabric8/docker-client)


Docker client for the Docker API version 1.21.

## Features

- Rich DSL
- Unix Domain Socket
- Docker Server Mock
- OSGi support (includes Apache Karaf Feature Repository)


## Configuring the client

    Config config = new ConfigBuilder()
                            .withDockerUrl("http://someip:2375")
                            .build();

    DockerClient client = new DefaultDockerClient(config);

The config instance can be configured via ConfigBuilder or via enviornment variables (e.g. DOCKER_HOST). In the later case, you can directly isntantiate the DefaultDockerClient:

    DockerClient client = new DefaultDockerClient();


### Images

#### List Images

To list all images:

    client.image().list().allImages();

To list images without the intermediate

    client.image().list().endImages();

To add some key/values as filters:

    client.image().list().filters('someKey','someVal').allImages();

#### Building an image

To create an image from a folder:

    client.build().withRepositoryName("myimage").fromFolder(".")

To create an image from an external tarball:

    client.build().withRepositoryName("myimage").fromTar("/path/to/tarball")

or using a stream to the tarball:

    client.build().withRepositoryName("myimage").fromTar(tarballInputStream)

Build operations are executed asynchronously, so you may need to use callbacks for, progress, success or failure:

    OutputHandle handle = client.build().withRepositoryName("my/image")
                                .usingListener(new EventListener() {
                                    @Override
                                    public void onSuccess(String message) {
                                        System.out.println("Success:" + message);
                                    }

                                    @Override
                                    public void onError(String messsage) {
                                        System.err.println("Failure:" +messsage);
                                    }

                                    @Override
                                    public void onEvent(String event) {
                                        System.out.println(event);
                                    }
                                })
                                .fromFolder(".");

If you just need to grab the output rather than using callbacks you can either redirect output to the handle:

    OutputHandle handle = client.build().withRepositoryName("my/image")
                                 .redirectingOutput()
                                 .fromFolder(".");

    OutputStream out = handle.getOutput();
    ...
    handle.close();

or even bring you own stream:

         OutputHandle handle = client.build().withRepositoryName("my/image")
                                     .writingOutput(System.out)
                                     .fromFolder(".");

There are tons of options as described in the docker remote api and all of them are exposed as methods in the DSL.


#### Tagging an image

To tag an image:

    client.image().withName("my/image").tag().inRepository("192.168.1.10:5000/my/image").withTagName("v1");

In the example above we tag and image into a repository prefixed with a local docker registry address.

#### Pushing an image

To push a tag into the registry:

    client.image().withName("192.168.1.10:5000/my/image").push().withTag("v1").toRegistry();

#### Inspecting an image
    ImageInspect inspect = client.image().withName("my/image").inspect();

#### Pulling an image

    client.image().withName("192.168.1.10:5000/my/image").pull().withTag("v1").fromRegistry();

#### Deleting an image

    Boolean deleted = client.image().withName("192.168.1.10:5000/my/image").delete();
