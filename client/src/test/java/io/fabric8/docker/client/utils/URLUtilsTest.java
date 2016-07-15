package io.fabric8.docker.client.utils;

import org.junit.Test;

import static io.fabric8.docker.client.Config.HTTPS_PROTOCOL_PREFIX;
import static io.fabric8.docker.client.Config.HTTP_PROTOCOL_PREFIX;
import static org.junit.Assert.assertEquals;

public class URLUtilsTest {

    @Test
    public void givenUrlWithTcpSchemeWhenSwitchingToHttpSchemeThenRestOfUrlIsUnchanged() {
        String tcpUrl = "tcp://myhost:2376";
        assertEquals("http://myhost:2376", URLUtils.withProtocol(tcpUrl, HTTP_PROTOCOL_PREFIX));
    }

    @Test
    public void givenUrlWithTcpSchemeWhenSwitchingToHttpsSchemeThenRestOfUrlIsUnchanged() {
        String tcpUrl = "tcp://myhost:2376";
        assertEquals("https://myhost:2376", URLUtils.withProtocol(tcpUrl, HTTPS_PROTOCOL_PREFIX));
    }

}
