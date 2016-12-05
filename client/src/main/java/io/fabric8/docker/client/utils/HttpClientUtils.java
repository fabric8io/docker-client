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
package io.fabric8.docker.client.utils;

import okhttp3.Authenticator;
import okhttp3.Challenge;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.unix.UnixSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.fabric8.docker.client.utils.Utils.isNotNullOrEmpty;

public class HttpClientUtils {

    //Let's fool okhttp to work with non http schemes!
    //How?
    //We pass a fake url and a custom socket factory.
    private static final String UNIX_FAKE_URL = "http://localhost:80";

    private static final String UNIX_SCHEME = "unix";
    private static final String FILE_SCHEME = "file";

    public static OkHttpClient createHttpClient(final Config config) {
        try {
            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

            // Follow any redirects
            httpClientBuilder.followRedirects(true);
            httpClientBuilder.followSslRedirects(true);

            if (config.isTrustCerts()) {
                httpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                });
            }

            if (usesUnixSocket(config)) {
                URL masterURL = new URL(config.getDockerUrl().replaceFirst(UNIX_SCHEME, FILE_SCHEME));
                httpClientBuilder.socketFactory(new UnixSocketFactory(masterURL.getFile()));
                config.setDockerUrl(UNIX_FAKE_URL);
            }

            TrustManager[] trustManagers = SSLUtils.trustManagers(config);
            KeyManager[] keyManagers = SSLUtils.keyManagers(config);

            if (keyManagers != null || trustManagers != null || config.isTrustCerts()) {
                X509TrustManager trustManager = null;
                if (trustManagers != null && trustManagers.length == 1) {
                    trustManager = (X509TrustManager) trustManagers[0];
                }

                try {
                    SSLContext sslContext = SSLUtils.sslContext(keyManagers, trustManagers, config.isTrustCerts());
                    httpClientBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
                } catch (GeneralSecurityException e) {
                    throw new AssertionError(); // The system has no TLS. Just give up.
                }
            } else {
                SSLContext context = SSLContext.getInstance("TLSv1.2");
                context.init(keyManagers, trustManagers, null);
                httpClientBuilder.sslSocketFactory(context.getSocketFactory(), (X509TrustManager) trustManagers[0]);
            }

            if (isNotNullOrEmpty(config.getUsername()) && isNotNullOrEmpty(config.getPassword())) {
                httpClientBuilder.authenticator(new Authenticator() {

                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        List<Challenge> challenges = response.challenges();
                        Request request = response.request();
                        HttpUrl url = request.url();
                        for (int i = 0, size = challenges.size(); i < size; i++) {
                            Challenge challenge = challenges.get(i);
                            if (!"Basic".equalsIgnoreCase(challenge.scheme())) continue;

                            String credential = Credentials.basic(config.getUsername(), config.getPassword());
                            return request.newBuilder()
                                    .header("Authorization", credential)
                                    .build();
                        }
                        return null;
                    }
                });
            } else if (config.getOauthToken() != null) {
                httpClientBuilder.interceptors().add(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request authReq = chain.request().newBuilder().addHeader("Authorization", "Bearer " + config.getOauthToken()).build();
                        return chain.proceed(authReq);
                    }
                });
            }

            Logger reqLogger = LoggerFactory.getLogger(HttpLoggingInterceptor.class);
            if (reqLogger.isTraceEnabled()) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClientBuilder.networkInterceptors().add(loggingInterceptor);
            }

            if (config.getConnectionTimeout() > 0) {
                httpClientBuilder.connectTimeout(config.getConnectionTimeout(), TimeUnit.MILLISECONDS);
            }

            if (config.getRequestTimeout() > 0) {
                httpClientBuilder.readTimeout(config.getRequestTimeout(), TimeUnit.MILLISECONDS);
            }

            // Only check proxy if it's a full URL with protocol
            if (config.getDockerUrl().toLowerCase().startsWith(Config.HTTP_PROTOCOL_PREFIX) || config.getDockerUrl().startsWith(Config.HTTPS_PROTOCOL_PREFIX)) {
                try {
                    URL proxyUrl = getProxyUrl(config);
                    if (proxyUrl != null) {
                        httpClientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort())));
                    }
                } catch (MalformedURLException e) {
                    throw new DockerClientException("Invalid proxy server configuration", e);
                }
            }

            return httpClientBuilder.build();
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    private static URL getProxyUrl(Config config) throws MalformedURLException {
        URL master = new URL(config.getDockerUrl());
        String host = master.getHost();
        for (String noProxy : config.getNoProxy()) {
            if (host.endsWith(noProxy)) {
                return null;
            }
        }
        String proxy = config.getHttpsProxy();
        if (master.getProtocol().equals("http")) {
            proxy = config.getHttpProxy();
        }
        if (proxy != null) {
            return new URL(proxy);
        }
        return null;
    }

    private static boolean usesUnixSocket(Config config) {
        return config.getDockerUrl().startsWith(UNIX_SCHEME);
    }
}
