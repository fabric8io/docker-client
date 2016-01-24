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

package io.fabric8.docker.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.api.model.DockerConfig;
import io.fabric8.docker.client.utils.SSLUtils;
import io.fabric8.docker.client.utils.URLUtils;
import io.fabric8.docker.client.utils.Utils;
import io.sundr.builder.annotations.Buildable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final MapType AUTHCONFIG_TYPE = JSON_MAPPER.getTypeFactory().constructMapType(HashMap.class, String.class, AuthConfig.class);

    public static final String DOCKER_AUTH_DOCKERCFG_ENABLED = "docker.auth.dockercfg.enabled";
    public static final String DOCKER_DOCKERCFG_FILE = "docker.auth.dockercfg.path";

    public static final String DOCKER_HOST = "docker.host";


    public static String TCP_PROTOCOL_PREFIX = "tcp://";
    public static String HTTP_PROTOCOL_PREFIX = "http://";
    public static String HTTPS_PROTOCOL_PREFIX = "https://";

    private boolean trustCerts;
    private String masterUrl;
    private String caCertFile;
    private String caCertData;
    private String clientCertFile;
    private String clientCertData;
    private String clientKeyFile;
    private String clientKeyData;
    private String clientKeyAlgo = "RSA";
    private String clientKeyPassphrase = "changeit";
    private String username;
    private String password;
    private String oauthToken;
    private int imageBuildTimeout = 10 * 60 * 1000;
    private int imagePushTimeout = 10 * 60 * 1000;
    private int imageSearchTimeout = 3 * 60 * 1000;
    private int connectionTimeout = 10 * 1000;
    private int requestTimeout = 30 * 1000;
    private String httpProxy;
    private String httpsProxy;
    private String[] noProxy;
    private Map<String, AuthConfig> authConfigs;

    public Config() {
        tryDockerConfig(this);
    }

    @Buildable
    public Config(boolean trustCerts, String masterUrl, String caCertFile, String caCertData, String clientCertFile, String clientCertData, String clientKeyFile, String clientKeyData, String clientKeyAlgo, String clientKeyPassphrase, String username, String password, String oauthToken, int imageBuildTimeout, int imagePushTimeout, int imageSearchTimeout, int connectionTimeout, int requestTimeout, String httpProxy, String httpsProxy, String[] noProxy, Map<String, AuthConfig> authConfigs) {
        this();
        this.trustCerts = trustCerts;
        this.masterUrl = masterUrl;
        this.caCertFile = caCertFile;
        this.caCertData = caCertData;
        this.clientCertFile = clientCertFile;
        this.clientCertData = clientCertData;
        this.clientKeyFile = clientKeyFile;
        this.clientKeyData = clientKeyData;
        this.clientKeyAlgo = clientKeyAlgo;
        this.clientKeyPassphrase = clientKeyPassphrase;
        this.username = username;
        this.password = password;
        this.oauthToken = oauthToken;
        this.imageBuildTimeout = imageBuildTimeout;
        this.imagePushTimeout = imagePushTimeout;
        this.imageSearchTimeout = imageSearchTimeout;
        this.connectionTimeout = connectionTimeout;
        this.requestTimeout = requestTimeout;
        this.httpProxy = httpProxy;
        this.httpsProxy = httpsProxy;
        this.noProxy = noProxy;

        if (authConfigs != null && !authConfigs.isEmpty()) {
            this.authConfigs = authConfigs;
        } else {
            this.authConfigs = new HashMap<>();
        }

        if (masterUrl == null) {
            this.masterUrl = Utils.getSystemPropertyOrEnvVar(DOCKER_HOST);
            if (this.masterUrl != null && this.masterUrl.startsWith(TCP_PROTOCOL_PREFIX)) {
                if (SSLUtils.isHttpsAvailable(this)) {
                    this.masterUrl = URLUtils.withProtocol(this.masterUrl, HTTPS_PROTOCOL_PREFIX);
                } else {
                    this.masterUrl = URLUtils.withProtocol(this.masterUrl, HTTP_PROTOCOL_PREFIX);
                }
            }
        }
    }

    private boolean tryDockerConfig(Config config) {
        if (Utils.getSystemPropertyOrEnvVar(DOCKER_AUTH_DOCKERCFG_ENABLED, true)) {
            String dockerConfig = Utils.getSystemPropertyOrEnvVar(DOCKER_DOCKERCFG_FILE, Paths.get(System.getProperty("user.home", "."), ".docker", "config.json").toFile().getAbsolutePath());
            File dockerConfigFile = new File((dockerConfig));
            boolean dockerConfigExists = Files.isRegularFile(dockerConfigFile.toPath());
            if (dockerConfigExists) {
                try {
                    DockerConfig cfg = JSON_MAPPER.readValue(dockerConfigFile, DockerConfig.class);
                    config.setAuthConfigs(cfg.getAuths());
                } catch (IOException e) {
                    LOGGER.error("Could not load kube config file from {}", dockerConfig, e);
                }
            }
        }
        return false;
    }

    public boolean isTrustCerts() {
        return trustCerts;
    }

    public void setTrustCerts(boolean trustCerts) {
        this.trustCerts = trustCerts;
    }

    public String getMasterUrl() {
        return masterUrl;
    }

    public void setMasterUrl(String masterUrl) {
        this.masterUrl = masterUrl;
    }

    public String getCaCertFile() {
        return caCertFile;
    }

    public void setCaCertFile(String caCertFile) {
        this.caCertFile = caCertFile;
    }

    public String getCaCertData() {
        return caCertData;
    }

    public void setCaCertData(String caCertData) {
        this.caCertData = caCertData;
    }

    public String getClientCertFile() {
        return clientCertFile;
    }

    public void setClientCertFile(String clientCertFile) {
        this.clientCertFile = clientCertFile;
    }

    public String getClientCertData() {
        return clientCertData;
    }

    public void setClientCertData(String clientCertData) {
        this.clientCertData = clientCertData;
    }

    public String getClientKeyFile() {
        return clientKeyFile;
    }

    public void setClientKeyFile(String clientKeyFile) {
        this.clientKeyFile = clientKeyFile;
    }

    public String getClientKeyData() {
        return clientKeyData;
    }

    public void setClientKeyData(String clientKeyData) {
        this.clientKeyData = clientKeyData;
    }

    public String getClientKeyAlgo() {
        return clientKeyAlgo;
    }

    public void setClientKeyAlgo(String clientKeyAlgo) {
        this.clientKeyAlgo = clientKeyAlgo;
    }

    public String getClientKeyPassphrase() {
        return clientKeyPassphrase;
    }

    public void setClientKeyPassphrase(String clientKeyPassphrase) {
        this.clientKeyPassphrase = clientKeyPassphrase;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public int getImageBuildTimeout() {
        return imageBuildTimeout;
    }

    public void setImageBuildTimeout(int imageBuildTimeout) {
        this.imageBuildTimeout = imageBuildTimeout;
    }

    public int getImagePushTimeout() {
        return imagePushTimeout;
    }

    public void setImagePushTimeout(int imagePushTimeout) {
        this.imagePushTimeout = imagePushTimeout;
    }

    public int getImageSearchTimeout() {
        return imageSearchTimeout;
    }

    public void setImageSearchTimeout(int imageSearchTimeout) {
        this.imageSearchTimeout = imageSearchTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(String httpProxy) {
        this.httpProxy = httpProxy;
    }

    public String getHttpsProxy() {
        return httpsProxy;
    }

    public void setHttpsProxy(String httpsProxy) {
        this.httpsProxy = httpsProxy;
    }

    public String[] getNoProxy() {
        return noProxy;
    }

    public void setNoProxy(String[] noProxy) {
        this.noProxy = noProxy;
    }

    public Map<String, AuthConfig> getAuthConfigs() {
        return authConfigs;
    }

    public void setAuthConfigs(Map<String, AuthConfig> authConfigs) {
        this.authConfigs = authConfigs;
    }
}
