package io.fabric8.docker.client.test;

import org.junit.Test;


import io.fabric8.docker.api.model.EditableNetworkCreate;
import io.fabric8.docker.api.model.NetworkCreateBuilder;
import io.fabric8.docker.api.model.NetworkCreateResponse;
import io.fabric8.docker.api.model.NetworkCreateResponseBuilder;
import io.fabric8.docker.api.model.NetworkResource;
import io.fabric8.docker.api.model.NetworkResourceBuilder;
import io.fabric8.docker.client.DockerClient;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NetworkOperationTest extends DockerMockServerTestBase  {

    @Test
    public void testNetworkList() {

        List<NetworkResource> expected = new ArrayList<>();
        expected.add(new NetworkResourceBuilder().withName("net1").build());
        expected.add(new NetworkResourceBuilder().withName("net2").build());
        expected.add(new NetworkResourceBuilder().withName("net3").build());

        expect().withPath("/networks")
                .andReturn(200, expected)
                .always();

        DockerClient client = getClient();

        List<NetworkResource> actual = client.network().list().all();
        assertEquals(expected,actual);
    }

    @Test
    public void testNetworkCreate() {

        NetworkCreateResponse expected = new NetworkCreateResponseBuilder().withId("11111").build();

        expect().post().withPath("/networks/create")
                .andReturn(201, expected).once();

        EditableNetworkCreate net1 = new NetworkCreateBuilder().withName("net1").build();

        DockerClient client = getClient();

        NetworkCreateResponse actual = client.network().create(net1);
        assertEquals(expected, actual);
    }

    @Test
    public void testNetworkInspect() {

        NetworkResource expected = new NetworkResourceBuilder().withName("net1").build();

        expect().withPath("/networks/net1").andReturn(200,expected).always();

        DockerClient client = getClient();

        NetworkResource actual = client.network().withName("net1").inspect();
        assertEquals(expected,actual);
    }

    @Test
    public void testNetworkRemove() {


        expect().delete().withPath("/networks/net1")
                .andReturn(204, "").once();

        DockerClient client = getClient();

        assertTrue(client.network().withName("net1").delete());
    }
}