package io.fabric8.docker.client;

import com.sun.tools.doclets.internal.toolkit.util.DocFinder;
import io.fabric8.docker.api.model.Container;
import io.fabric8.docker.api.model.ContainerCreateResponse;
import io.fabric8.docker.api.model.ContainerProcessList;
import io.fabric8.docker.client.utils.InputStreamPumper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultDockerClientTest {

    @Test
    public void testContainerOps() throws IOException, InterruptedException {
        DockerClient client = new DefaultDockerClient();

        ContainerCreateResponse response = client.container().createNew().withImage("mongo").withCmd("start.sh").done();

        //1. list all containers
        System.out.println("All");
        for (Container container :client.container().list().all()) {
            System.out.println(container);
        }

        //2. list running containers
        System.out.println("Running");
        for (Container container :client.container().list().running()) {
            System.out.println(container);
        }

        //3. list running containers
        System.out.println("Limit 1 - Running");
        for (Container container :client.container().list().limit(1).running()) {
            System.out.println(container);
        }

        //3. list running containers
        System.out.println("Using Filters");
        for (Container container : client.container().list().limit(1).filters("status", "running").all()) {
            System.out.println(container);
        }

        Container container = client.container().list().limit(1).running().get(0);

        //4. List container processes
        System.out.println("Top");
        ContainerProcessList processList = client.container().withName(container.getId()).top();
        System.out.println(processList);

        //5. Attach to the first running container
        OutputHandle handle = client.container().withName(container.getId()).attach()
                .redirectingOutput()
                .redirectingError()
                .stream();

        Thread.sleep(10 * 1000);

        handle.close();
    }

    @Test
    public void testImageOps() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        DockerClient client = new DefaultDockerClient();
        InputStream in = client.images().build()
                .withRepositoryName("test1")
                .fromFolder(getClass().getClassLoader().getResource("image1").getFile());


        InputStreamPumper pumper = new InputStreamPumper(in, new Callback<byte[]>() {
            @Override
            public void call(byte[] input) {
                System.out.println(new String(input));
            }
        });

        executorService.submit(pumper);

    }

}