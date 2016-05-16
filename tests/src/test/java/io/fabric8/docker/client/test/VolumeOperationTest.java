package io.fabric8.docker.client.test;

import io.fabric8.docker.api.model.*;
import io.fabric8.docker.api.model.VolumeBuilder;
import io.fabric8.docker.api.model.VolumeCreateRequestBuilder;
import io.fabric8.docker.api.model.VolumesListResponseBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.docker.client.DockerClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VolumeOperationTest extends DockerMockServerTestBase {

    @Test
    public void testVolumeList() {
        List<Volume> expected = new ArrayList<>();
        expected.add(new VolumeBuilder().withName("vol1").build());
        expected.add(new VolumeBuilder().withName("vol2").build());
        expected.add(new VolumeBuilder().withName("vol3").build());

        VolumesListResponse response = new VolumesListResponseBuilder().withVolumes(expected).build();


        expect().withPath("/volumes").andReturn(200,response).always();

        DockerClient client = getClient();

        List<Volume> actual = client.volume().list().all();
        assertEquals(expected,actual);
    }

    @Test
    public void testVolumeCreate() {
        VolumeCreateRequest request = new VolumeCreateRequestBuilder().withName("vol1").build();

        Volume expected = new VolumeBuilder().withName("vol1").build();

        expect().post().withPath("/volumes/create").andReturn(201,expected).once();

        DockerClient client = getClient();
        Volume actual = client.volume().create(request);
        assertEquals(expected,actual);
    }

    @Test
    public void testVolumeInspect() {
        Volume expected = new VolumeBuilder().withName("vol1").build();

        expect().withPath("/volumes/vol1").andReturn(200,expected).always();

        DockerClient client = getClient();
        Volume actual = client.volume().withName("vol1").inspect();
        assertEquals(expected, actual);
    }

    @Test
    public void testVolumeRemove() {

        expect().delete().withPath("/volumes/vol1").andReturn(204,"").once();

        DockerClient client = getClient();
        assertTrue(client.volume().withName("vol1").delete());
    }
}
