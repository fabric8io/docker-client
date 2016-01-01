package io.fabric8.docker.client;

import java.io.Closeable;
import java.io.InputStream;

public interface OutputHandle extends Closeable {

    InputStream getOutput();

    InputStream getError();

}
