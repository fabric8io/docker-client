package io.fabric8.docker.examples;

import io.fabric8.docker.api.model.ContainerCreateResponse;
import io.fabric8.docker.api.model.ContainerExecCreateResponse;
import io.fabric8.docker.api.model.ExecConfig;
import io.fabric8.docker.api.model.ExecConfigBuilder;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.client.DockerClientException;
import java.io.IOException;
import java.io.InputStream;

public class ContainerDownloadFromExample {
    public static void main(String args[]) throws InterruptedException, IOException {

        if (args.length < 2) {
            System.err.println("Usage: ContainerLogsExample <docker url>");
            System.err.println("Optionally: ContainerLogsExample <docker url> <container id>");
            return;
        }

        String dockerUrl = args[0];
        String image = args[1];

        Config config = new ConfigBuilder()
            .withDockerUrl(dockerUrl)
            .build();

        DockerClient client = new DefaultDockerClient(config);
        try {
            ContainerCreateResponse container = client.container().createNew()
                .withName("example")
                .withImage(image)
                .done();

            if (client.container().withName(container.getId()).start()) {
                System.out.println("Container started!");
            } else {
                throw new DockerClientException("Failed to start container.");
            }

            final String[] commands = {"sh", "-c", "echo 'Hello World' >> /hello_world.txt"};
            ExecConfig execConfig = new ExecConfigBuilder().addToCmd(commands).withDetach(true).build();

            final ContainerExecCreateResponse exec = client.container().withName(container.getId()).exec(execConfig);

            client.exec().withName(exec.getId()).start();

            final InputStream inputStream =
                client.container().withName(container.getId()).archive().downloadFrom("/hello_world.txt");

            if (client.container().withName(container.getId()).stop()) {
                System.out.println("Container stopped!");
            } else {
                throw new DockerClientException("Failed to stop container.");
            }

            client.container().withName(container.getId()).remove();
        } finally {
            client.close();
        }
    }
}
