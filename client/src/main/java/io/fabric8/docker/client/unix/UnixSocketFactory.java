/*
 * Copyright (C) 2016 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.fabric8.docker.client.unix;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.SocketFactory;

import jnr.unixsocket.UnixSocketAddress;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

public class UnixSocketFactory extends SocketFactory {

    private final String path;
    private final boolean useJnrUnixSocket;

    public UnixSocketFactory(String path, boolean useJnrUnixSocket) {
        this.path = path;
        this.useJnrUnixSocket = useJnrUnixSocket;
    }

    @Override
    public Socket createSocket() throws IOException {
        return useJnrUnixSocket ?
            new JnrUnixSocket(new UnixSocketAddress(new File(path))) :
            new AfUnixSocket(AFUNIXSocket.newInstance(), new AFUNIXSocketAddress(new File(path)));
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
        throw new UnsupportedOperationException();
    }
}
