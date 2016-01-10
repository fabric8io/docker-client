package io.fabric8.docker.dsl;

import java.io.Closeable;
import java.io.InputStream;

public interface OutputHandle extends Closeable {

    InputStream getOutput();

}
