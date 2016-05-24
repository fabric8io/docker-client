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

import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

public class AfUnixSocket extends Socket {

    private AFUNIXSocket delegate;
    private AFUNIXSocketAddress fixed;
    private final Queue<Runnable> optionsToSet = new ArrayDeque<Runnable>();

    public AfUnixSocket(AFUNIXSocket delegate, AFUNIXSocketAddress fixed) {
        this.fixed = fixed;
        this.delegate = delegate;
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        delegate.bind(fixed);
        applySocketOptions();
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        delegate.connect(fixed);
        applySocketOptions();
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        delegate.connect(fixed, timeout);
        applySocketOptions();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public static boolean isSupported() {
        return AFUNIXSocket.isSupported();
    }

    @Override
    public InetAddress getInetAddress() {
        return delegate.getInetAddress();
    }

    @Override
    public InetAddress getLocalAddress() {
        return delegate.getLocalAddress();
    }

    @Override
    public int getPort() {
        return delegate.getPort();
    }

    @Override
    public int getLocalPort() {
        return delegate.getLocalPort();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return fixed;
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return delegate.getLocalSocketAddress();
    }

    @Override
    public SocketChannel getChannel() {
        return delegate.getChannel();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FilterInputStream(delegate.getInputStream()) {
            @Override
            public void close() throws IOException {
                shutdownInput();
            }
        };
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FilterOutputStream(delegate.getOutputStream()) {
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                out.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                shutdownOutput();
            }
        };
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        delegate.setTcpNoDelay(on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return delegate.getTcpNoDelay();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        delegate.setSoLinger(on, linger);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return delegate.getSoLinger();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        delegate.sendUrgentData(data);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        delegate.setOOBInline(on);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return delegate.getOOBInline();
    }

    @Override
    public void setSoTimeout(final int timeout) throws SocketException {
        optionsToSet.add(new Runnable() {
            @Override
            public void run() {
                try {
                    delegate.setSoTimeout(timeout);
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public int getSoTimeout() throws SocketException {
        return delegate.getSoTimeout();
    }

    @Override
    public void setSendBufferSize(final int size) throws SocketException {
        optionsToSet.add(new Runnable() {
            @Override
            public void run() {
                try {
                    delegate.setSendBufferSize(size);
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        return delegate.getSendBufferSize();
    }

    @Override
    public void setReceiveBufferSize(final int size) throws SocketException {
        optionsToSet.add(new Runnable() {
            @Override
            public void run() {
                try {
                    delegate.setReceiveBufferSize(size);
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        return delegate.getReceiveBufferSize();
    }

    @Override
    public void setKeepAlive(final boolean on) throws SocketException {
        optionsToSet.add(new Runnable() {
            @Override
            public void run() {
                try {
                    delegate.setKeepAlive(on);
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return delegate.getKeepAlive();
    }

    @Override
    public void setTrafficClass(final int tc) throws SocketException {
        optionsToSet.add(new Runnable() {
            @Override
            public void run() {
                try {
                    delegate.setTrafficClass(tc);
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return delegate.getTrafficClass();
    }

    @Override
    public void setReuseAddress(final boolean on) throws SocketException {
        optionsToSet.add(new Runnable() {
            @Override
            public void run() {
                try {
                    delegate.setReuseAddress(on);
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return delegate.getReuseAddress();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void shutdownInput() throws IOException {
        delegate.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        delegate.shutdownOutput();
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public boolean isBound() {
        return delegate.isBound();
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return delegate.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return delegate.isOutputShutdown();
    }

    public static void setSocketImplFactory(SocketImplFactory fac) throws IOException {
        Socket.setSocketImplFactory(fac);
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        delegate.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    private void applySocketOptions() {
        for (Runnable runnable : optionsToSet) {
                runnable.run();
        }
    }
}
