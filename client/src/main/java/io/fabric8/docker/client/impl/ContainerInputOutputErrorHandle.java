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

package io.fabric8.docker.client.impl;

import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketListener;
import io.fabric8.docker.api.model.Callback;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.InputOutputErrorHandle;
import io.fabric8.docker.client.utils.InputStreamPumper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ContainerInputOutputErrorHandle extends ContainerOutputHandle implements InputOutputErrorHandle, WebSocketListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerInputOutputErrorHandle.class);

    private final InputStream in;
    private final PipedOutputStream input;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final InputStreamPumper pumper;


    public ContainerInputOutputErrorHandle(InputStream in, OutputStream out, OutputStream err, PipedOutputStream inputPipe, PipedInputStream outputPipe, PipedInputStream errorPipe) {
        super(out,err,outputPipe,errorPipe);
        this.in = inputStreamOrPipe(in, inputPipe);
        this.input = inputPipe;
        this.pumper = new InputStreamPumper(this.in, new Callback<byte[], Void>() {
            @Override
            public Void call(byte[] data) {
                try {
                    send(data);
                } catch (Exception e) {
                    //
                }
                return null;
            }
        });
    }

    @Override
    public void close() {
        pumper.close();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (Throwable t) {
            throw DockerClientException.launderThrowable(t);
        }
        super.close();
    }



    public OutputStream getInput() {
        return input;
    }

    private void send(byte[] bytes) throws IOException {
        if (bytes.length > 0) {
            WebSocket ws = webSocketRef.get();
            if (ws != null) {
                byte[] toSend = new byte[bytes.length + 1];
                toSend[0] = 0;
                System.arraycopy(bytes, 0, toSend, 1, bytes.length);
                ws.sendMessage(RequestBody.create(WebSocket.BINARY, toSend));
            }
        }
    }

    private static InputStream inputStreamOrPipe(InputStream stream, PipedOutputStream out) {
        if (stream != null) {
            return stream;
        } else if (out != null) {
            return new PipedInputStream();
        } else {
            return null;
        }
    }
}
