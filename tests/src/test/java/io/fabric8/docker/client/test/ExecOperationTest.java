package io.fabric8.docker.client.test;

import org.junit.Test;

import io.fabric8.docker.api.model.ContainerExecCreateResponse;
import io.fabric8.docker.api.model.ContainerExecCreateResponseBuilder;
import io.fabric8.docker.api.model.ContainerInspect;
import io.fabric8.docker.api.model.ContainerInspectBuilder;
import io.fabric8.docker.api.model.ExecConfig;
import io.fabric8.docker.api.model.ExecConfigBuilder;
import io.fabric8.docker.client.DockerClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExecOperationTest extends DockerMockServerTestBase {

    @Test
    public void testExecCreate() {
        ContainerExecCreateResponse expected = new ContainerExecCreateResponseBuilder().withId("111111").build();
        ExecConfig config = new ExecConfigBuilder().addToCmd("ls").withDetach(true).build();

        expect().post().withPath("/containers/con1/exec").andReturn(201,expected).once();

        DockerClient client = getClient();
        ContainerExecCreateResponse actual = client.container().withName("con1").exec(config);
        assertEquals(expected,actual);
    }

    @Test
    public void testExecStart() {
        expect().post().withPath("/exec/11111/start").andReturn(200,"").once();

        DockerClient client = getClient();
        assertTrue(client.exec().withName("11111").start());
    }

    @Test
    public void testExecInspect() {
        ContainerInspect expected = new ContainerInspectBuilder().withId("11111").withName("con1").build();

        expect().withPath("/exec/11111/json").andReturn(200,expected).always();

        DockerClient client = getClient();
        ContainerInspect actual = client.exec().withName("11111").inspect();
        assertEquals(expected, actual);
    }

}
