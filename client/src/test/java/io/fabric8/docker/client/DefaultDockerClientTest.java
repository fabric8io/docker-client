package io.fabric8.docker.client;

import io.fabric8.docker.api.model.Container;
import io.fabric8.docker.api.model.ContainerCreateResponse;
import io.fabric8.docker.api.model.ContainerProcessList;
import io.fabric8.docker.api.model.SearchResult;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
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
    public void testImageBuildTagPush() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        DockerClient client = new DefaultDockerClient();
        final CountDownLatch buildDone = new CountDownLatch(1);
        final CountDownLatch pushDone = new CountDownLatch(1);

        client.image().pull().usingListener(new EventListener() {
            @Override
            public void onSuccess(String message) {
                System.out.println(message);
            }

            @Override
            public void onError(String message) {
                System.out.println(message);
            }

            @Override
            public void onEvent(String event) {
                System.out.println(event);
            }
        }).fromImage("maven");

        OutputHandle handle = client.image().build()
                .withRepositoryName("test1")
                .usingListener(new EventListener() {
                    @Override
                    public void onSuccess(String message) {
                        System.out.println("Success:" + message);
                        buildDone.countDown();
                    }

                    @Override
                    public void onError(String messsage) {
                        System.err.println("Failure:" +messsage);
                        buildDone.countDown();
                    }

                    @Override
                    public void onEvent(String event) {
                        System.out.println(event);
                    }
                })
                .fromFolder(getClass().getClassLoader().getResource("image1").getFile());

        buildDone.await();
        handle.close();


        client.image().withName("test1").tag().inRepository("172.30.128.236:5000/test1").force().withTagName("v1");

        handle = client.image().withName("172.30.128.236:5000/test1").push().usingListener(new EventListener() {
            @Override
            public void onSuccess(String message) {
                System.out.println("Success:" + message);
                pushDone.countDown();
            }

            @Override
            public void onError(String message) {
                System.out.println("Error:" + message);
                pushDone.countDown();
            }

            @Override
            public void onEvent(String event) {
                System.out.println(event);

            }
        }).toRegistry();

        pushDone.await();
        handle.close();

        for (SearchResult r : client.image().search("test1")) {
            System.out.println(r);
        }
    }

}